package io.crnk.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.meta.model.*;
import io.crnk.meta.provider.MetaProvider;
import io.crnk.meta.provider.MetaProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MetaLookup {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetaLookup.class);

	private static final String BASE_ID_PREFIX = "base.";

	private MultivaluedMap<Type, MetaElement> typeElementsMap = new MultivaluedMap<>();

	private ConcurrentHashMap<String, MetaElement> idElementMap = new ConcurrentHashMap<>();

	private Set<Class<?>> primitiveTypes = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());

	private List<MetaProvider> providers = new CopyOnWriteArrayList<>();

	private LinkedList<MetaElement> initializationQueue = new LinkedList<>();

	private boolean adding = false;

	private MetaProviderContext context;

	private Map<String, String> packageIdMapping = new HashMap<>();

	private boolean discovered;

	private ModuleContext moduleContext;

	public MetaLookup() {
		registerPrimitiveType(String.class);
		registerPrimitiveType(Number.class);
		registerPrimitiveType(Boolean.class);
		registerPrimitiveType(Integer.class);
		registerPrimitiveType(Short.class);
		registerPrimitiveType(Byte.class);
		registerPrimitiveType(Long.class);
		registerPrimitiveType(Float.class);
		registerPrimitiveType(Double.class);
		registerPrimitiveType(UUID.class);
		registerPrimitiveType(Date.class);
		registerPrimitiveType(Timestamp.class);
		registerPrimitiveType(JsonNode.class);
		registerPrimitiveType(byte[].class);
		registerPrimitiveType(boolean[].class);
		registerPrimitiveType(int[].class);
		registerPrimitiveType(short[].class);
		registerPrimitiveType(long[].class);
		registerPrimitiveType(double[].class);
		registerPrimitiveType(float[].class);

		context = new MetaProviderContext() {

			@Override
			public void add(MetaElement element) {
				MetaLookup.this.add(element);
			}

			@Override
			public MetaLookup getLookup() {
				return MetaLookup.this;
			}

			@Override
			public ModuleContext getModuleContext() {
				return moduleContext;
			}
		};

		putIdMapping("io.crnk.jpa.meta", "io.crnk.jpa");
		putIdMapping("io.crnk.meta.model", "io.crnk.meta");
		putIdMapping("io.crnk.meta.model.resource", "io.crnk.meta.resource");
	}

	private static String firstToLower(String name) {
		if (name.equals(JsonNode.class.getSimpleName())) {
			return "json";
		}
		if (name.equals("UUID")) {
			return "uuid";
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	public void setModuleContext(ModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}

	public Map<String, MetaElement> getMetaById() {
		return Collections.unmodifiableMap(idElementMap);
	}

	public void registerPrimitiveType(Class<?> clazz) {
		primitiveTypes.add(clazz);
	}

	public void addProvider(MetaProvider provider) {
		if (!providers.contains(provider)) {
			provider.init(context);
			providers.add(provider);
			for (MetaProvider dependency : provider.getDependencies()) {
				addProvider(dependency);
			}
		}
	}

	public MetaElement getMeta(Type type) {
		return getMeta(type, MetaElement.class);
	}

	public <T extends MetaElement> T getMeta(Type type, Class<T> metaClass) {
		return (T) getMetaInternal(type, metaClass, false);
	}

	public <T extends MetaElement> T getMeta(Type type, Class<T> metaClass, boolean nullable) {
		return (T) getMetaInternal(type, metaClass, nullable);
	}

	public MetaArrayType getArrayMeta(Type type, Class<? extends MetaElement> elementMetaClass) {
		return (MetaArrayType) getMetaInternal(type, elementMetaClass, false);
	}

	private MetaElement getMetaInternal(Type type, Class<? extends MetaElement> elementMetaClass, boolean nullable) {
		PreconditionUtil.assertNotNull("type must not be null", type);

		checkInitialized();

		MetaElement existingElement = getUniqueElementByType(type, elementMetaClass);
		if (existingElement == null) {
			synchronized (this) {
				existingElement = getUniqueElementByType(type, elementMetaClass);
				if (existingElement == null) {

					boolean wasInitializing = adding;
					if (!wasInitializing) {
						adding = true;
					}

					MetaElement allocatedMeta = allocateMeta(type, elementMetaClass, nullable);
					if (allocatedMeta != null) {
						add(allocatedMeta);
					}

					if (!wasInitializing) {
						initialize();
					}
					return allocatedMeta;
				}
			}
		}
		return existingElement;
	}

	private MetaElement getUniqueElementByType(Type type, Class<? extends MetaElement> elementMetaClass) {
		if (!typeElementsMap.containsKey(type)) {
			return null;
		}
		List<MetaElement> elements = typeElementsMap.getList(type);
		MetaElement result = null;
		for (MetaElement element : elements) {
			MetaElement meta = element;
			if (meta instanceof MetaType) {
				meta = ((MetaType) meta).getElementType();
			}

			if (elementMetaClass.isInstance(meta)) {
				if (result != null) {
					throw new IllegalStateException(
							"multiple elements found of type " + elementMetaClass + ": " + result + " vs " + element);
				}
				result = element;
			}
		}
		return result;
	}

	private MetaElement allocateMeta(Type type, Class<? extends MetaElement> metaClass, boolean nullable) {
		LOGGER.debug("allocate {}", type);

		if (type instanceof Class) {
			MetaElement meta = allocateMetaFromClass(type, metaClass);
			if (meta != null) {
				return meta;
			}
		}

		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			return allocateMetaFromParamerizedType(paramType, metaClass);
		}

		MetaElement meta = allocateMetaFromFactory(type, metaClass);
		if (meta != null) {
			return meta;
		}

		if (nullable) {
			return null;
		}
		throw new UnsupportedOperationException("unknown type " + type);
	}

	private MetaElement allocateMetaFromClass(Type type, Class<? extends MetaElement> metaClass) {
		Class<?> clazz = (Class<?>) type;
		if (clazz.isEnum() && metaClass.isAssignableFrom(MetaEnumType.class)) {
			MetaEnumType enumType = new MetaEnumType();
			enumType.setElementType(enumType);
			enumType.setImplementationType(type);
			enumType.setName(clazz.getSimpleName());
			for (Object literalObj : clazz.getEnumConstants()) {
				MetaLiteral literal = new MetaLiteral();
				literal.setName(literalObj.toString());
				literal.setParent(enumType, true);
			}
			return enumType;
		}
		clazz = mapPrimitiveType(clazz);
		if (isPrimitiveType(clazz) && metaClass.isAssignableFrom(MetaPrimitiveType.class)) {
			String id = BASE_ID_PREFIX + firstToLower(clazz.getSimpleName());

			MetaPrimitiveType primitiveType = (MetaPrimitiveType) idElementMap.get(id);
			if (primitiveType == null) {
				primitiveType = new MetaPrimitiveType();
				primitiveType.setElementType(primitiveType);
				primitiveType.setImplementationType(clazz);
				primitiveType.setName(firstToLower(clazz.getSimpleName()));
				primitiveType.setId(id);
			}
			return primitiveType;
		} else if (clazz.isArray()) {
			Class<?> elementClass = ((Class<?>) type).getComponentType();

			MetaType elementType = (MetaType) getMeta(elementClass, metaClass, true);
			if (elementType != null) {
				MetaArrayType arrayType = new MetaArrayType();
				arrayType.setName(elementType.getName() + "$Array");
				arrayType.setId(elementType.getId() + "$Array");
				arrayType.setImplementationType(type);
				arrayType.setElementType(elementType);
				return arrayType;
			}
		}
		return null;
	}

	private Class<?> mapPrimitiveType(Class<?> clazz) {
		if (clazz == byte.class) {
			return Byte.class;
		}
		if (clazz == short.class) {
			return Short.class;
		}
		if (clazz == int.class) {
			return Integer.class;
		}
		if (clazz == long.class) {
			return Long.class;
		}
		if (clazz == float.class) {
			return Float.class;
		}
		if (clazz == double.class) {
			return Double.class;
		}
		if (clazz == boolean.class) {
			return boolean.class;
		}
		if (clazz == ObjectNode.class) {
			return JsonNode.class;
		}
		return clazz;
	}

	private <T extends MetaElement> T allocateMetaFromFactory(Type type, Class<? extends MetaElement> metaClass) {
		for (MetaProvider factory : providers) {
			if (factory.accept(type, metaClass)) {
				return (T) factory.createElement(type);
			}
		}
		return null;
	}

	private MetaElement allocateMetaFromParamerizedType(ParameterizedType paramType,
														Class<? extends MetaElement> elementMetaClass) {
		if (paramType.getRawType() instanceof Class && Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
			PreconditionUtil.assertEquals("expected 2 type arguments", 2, paramType.getActualTypeArguments().length);
			MetaType keyType = (MetaType) getMeta(paramType.getActualTypeArguments()[0]);
			MetaType valueType = (MetaType) getMeta(paramType.getActualTypeArguments()[1], elementMetaClass, true);
			if (keyType != null && valueType != null) {
				MetaMapType mapMeta = new MetaMapType();

				boolean primitiveKey = keyType instanceof MetaPrimitiveType;
				boolean primitiveValue = valueType instanceof MetaPrimitiveType;
				if (primitiveKey || !primitiveValue) {
					mapMeta.setName(valueType.getName() + "$MappedBy$" + keyType.getName());
					mapMeta.setId(valueType.getId() + "$MappedBy$" + keyType.getName());
				} else {
					mapMeta.setName(keyType.getName() + "$Map$" + valueType.getName());
					mapMeta.setId(keyType.getId() + "$Map$" + valueType.getName());
				}

				mapMeta.setImplementationType(paramType);
				mapMeta.setKeyType(keyType);
				mapMeta.setElementType(valueType);
				return mapMeta;
			}
		} else if (paramType.getRawType() instanceof Class
				&& Collection.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
			return allocateMetaFromCollectionType(paramType, elementMetaClass);
		}
		return null;
	}

	private MetaElement allocateMetaFromCollectionType(ParameterizedType paramType,
													   Class<? extends MetaElement> elementMetaClass) {
		PreconditionUtil.assertEquals("expected single type argument", 1, paramType.getActualTypeArguments().length);
		MetaType elementType = (MetaType) getMeta(paramType.getActualTypeArguments()[0], elementMetaClass, true);
		if (elementType == null) {
			return null;
		}

		boolean isSet = Set.class.isAssignableFrom((Class<?>) paramType.getRawType());
		boolean isList = List.class.isAssignableFrom((Class<?>) paramType.getRawType());
		if (isSet) {
			MetaSetType metaSet = new MetaSetType();
			metaSet.setId(elementType.getId() + "$Set");
			metaSet.setName(elementType.getName() + "$Set");
			metaSet.setImplementationType(paramType);
			metaSet.setElementType(elementType);
			return metaSet;
		} else {
			PreconditionUtil.assertTrue("expected a list type", isList);
			MetaListType metaList = new MetaListType();
			metaList.setId(elementType.getId() + "$List");
			metaList.setName(elementType.getName() + "$List");
			metaList.setImplementationType(paramType);
			metaList.setElementType(elementType);
			return metaList;
		}
	}

	public boolean isPrimitiveType(Class<?> clazz) {
		clazz = mapPrimitiveType(clazz);

		if (clazz == Object.class) {
			return true;
		}

		if (clazz.getPackage() != null && clazz.getPackage().getName().equals("java.time")) {
			return true;
		}

		if (clazz.isPrimitive() || primitiveTypes.contains(clazz)) {
			return true;
		}

		for (Class<?> primitiveType : primitiveTypes) {
			if (primitiveType.isAssignableFrom(clazz)) {
				return true;
			}
		}
		return false;
	}

	protected void add(MetaElement element) {
		PreconditionUtil.assertNotNull("no name provided", element.getName());
		if (element instanceof MetaType) {
			MetaType typeElement = element.asType();
			Class<?> implClass = typeElement.getImplementationClass();
			if (!element.hasId()) {
				element.setId(computeIdPrefixFromPackage(implClass, element) + element.getName());
			}
		}

		if (!element.hasId() && element.getParent() != null) {
			element.setId(element.getParent().getId() + "." + element.getName());
		}

		if (idElementMap.get(element.getId()) != element) {
			LOGGER.debug("add {} of type {}", element.getId(), element.getClass().getSimpleName());

			// queue for initialization
			initializationQueue.add(element);

			// add to data structures
			if (element instanceof MetaType) {
				MetaType typeElement = element.asType();

				// check not alreay exists
				if (typeElementsMap.containsKey(typeElement.getImplementationType())) {
					List<MetaElement> existingElements = typeElementsMap.getList(typeElement.getImplementationType());
					for (MetaElement existingElement : existingElements) {
						if (existingElement.getId().equals(element.getId())) {
							throw new IllegalStateException(
									element.getId() + " already available: " + existingElement + " vs " + element);
						}
					}
				}

				typeElementsMap.add(typeElement.getImplementationType(), element);
			}
			MetaElement currentElement = idElementMap.get(element.getId());
			PreconditionUtil.assertNull(element.getId(), currentElement);
			idElementMap.put(element.getId(), element);

			// add children recursively
			for (MetaElement child : element.getChildren()) {
				add(child);
			}
		}
	}

	private String computeIdPrefixFromPackage(Class<?> implClass, MetaElement element) {
		Package implPackage = implClass.getPackage();
		if (implPackage == null && implClass.isArray()) {
			implPackage = implClass.getComponentType().getPackage();
		}
		if (implPackage == null) {
			throw new IllegalStateException(implClass.getName() + " does not belong to a package");
		}
		String packageName = implPackage.getName();
		StringBuilder idInfix = new StringBuilder(".");
		while (true) {

			String idMappingKey1 = toIdMappingKey(packageName, element.getClass());
			String idMappingKey2 = toIdMappingKey(packageName, null);

			String idPrefix = packageIdMapping.get(idMappingKey1);
			if (idPrefix == null) {
				idPrefix = packageIdMapping.get(idMappingKey2);
			}
			if (idPrefix != null) {
				return idPrefix + idInfix;
			}
			int sep = packageName.lastIndexOf('.');
			if (sep == -1) {
				break;
			}
			idInfix.append(packageName.substring(sep + 1));
			idInfix.append(".");
			packageName = packageName.substring(0, sep);
		}
		return implPackage.getName() + ".";
	}

	private void checkInitialized() {
		if (!discovered && !adding) {
			initialize();
		}
	}

	public void initialize() {
		LOGGER.debug("adding");
		adding = true;
		try {
			if (!discovered) {
				for (MetaProvider provider : providers) {
					provider.discoverElements();
				}
				discovered = true;
			}

			while (!initializationQueue.isEmpty()) {
				MetaElement element = initializationQueue.pollFirst();
				// initialize from roots down to decendants.
				if (element.getParent() == null) {
					initialize(element);
				}
			}
		} finally {
			LOGGER.debug("added");
			adding = false;
		}
	}

	private void initialize(MetaElement element) {
		LOGGER.debug("adding {}", element.getId());
		for (MetaProvider initializer : providers) {
			packageIdMapping.putAll(initializer.getIdMappings());
		}

		for (MetaProvider initializer : providers) {
			initializer.onInitializing(element);
		}

		for (MetaElement child : element.getChildren()) {
			initialize(child);
		}

		for (MetaProvider initializer : providers) {
			initializer.onInitialized(element);
		}
		LOGGER.debug("added {}", element.getId());
	}

	public List<MetaProvider> getProviders() {
		return providers;
	}

	public void putIdMapping(String packageName, String idPrefix) {
		packageIdMapping.put(packageName, idPrefix);
	}

	public void putIdMapping(String packageName, Class<? extends MetaElement> type, String idPrefix) {
		packageIdMapping.put(toIdMappingKey(packageName, type), idPrefix);
	}

	private String toIdMappingKey(String packageName, Class<? extends MetaElement> type) {
		if (type != null) {
			return packageName + "#" + type.getName();
		} else {
			return packageName;
		}
	}
}
