package io.crnk.meta.provider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.meta.model.MetaArrayType;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.MetaListType;
import io.crnk.meta.model.MetaLiteral;
import io.crnk.meta.model.MetaMapType;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.MetaSetType;
import io.crnk.meta.model.MetaType;

public abstract class MetaPartitionBase implements MetaPartition {

	protected MetaPartitionContext context;

	protected Map<Type, MetaElement> typeMapping = new HashMap<>();

	protected Set<Type> nonUniqueTypes = new HashSet<>();

	protected MetaPartition parent;

	@Override
	public void init(MetaPartitionContext context) {
		this.context = context;
	}

	public MetaElement getMeta(Type type) {
		MetaElement metaElement = typeMapping.get(type);
		if (metaElement == null && parent != null) {
			metaElement = parent.getMeta(type);
		}
		PreconditionUtil.verify(metaElement != null, "meta element not found for %s", type);
		return metaElement;
	}

	@Override
	public boolean hasMeta(Type type) {
		return typeMapping.containsKey(type) || parent != null && parent.hasMeta(type);
	}

	protected Optional<MetaElement> addElement(Type type, MetaElement element) {
		context.addElement(element);
		if (type != null && !nonUniqueTypes.contains(type)) {
			if (typeMapping.containsKey(type)) {
				nonUniqueTypes.add(type);
				typeMapping.remove(type);
			} else {
				typeMapping.put(type, element);
			}
		}
		return Optional.of(element);
	}

	public final Optional<MetaElement> allocateMetaElement(Type type) {
		if (typeMapping.containsKey(type)) {
			return Optional.of(typeMapping.get(type));
		}

		return context.runDiscovery(new Callable<Optional<MetaElement>>() {

			@Override
			public Optional<MetaElement> call() {
				if (parent != null) {
					Optional<MetaElement> element = parent.allocateMetaElement(type);
					if (element.isPresent()) {
						return element;
					}
				}

				if (typeMapping.containsKey(type)) {
					return Optional.of(typeMapping.get(type));
				}

				if(ClassUtils.getRawType(type) == Optional.class){
					Optional<MetaElement> element = allocateMetaElement(((ParameterizedType) type).getActualTypeArguments()[0]);
					if (element.isPresent()) {
						return element;
					}
				}

				if (type instanceof ParameterizedType) {
					Optional<MetaElement> element = allocateMap((ParameterizedType) type);
					if (element.isPresent()) {
						return element;
					}
				}

				Optional<MetaElement> element = allocateCollectionType(type);
				if (element.isPresent()) {
					return element;
				}

				element = allocateEnumType(type);
				if (element.isPresent()) {
					return element;
				}


				Optional<MetaElement> optElement = doAllocateMetaElement(type);
				PreconditionUtil.assertNotNull("must be not null", optElement);
				if (optElement.isPresent() && !optElement.get().hasId() && optElement.get() instanceof MetaType) {
					PreconditionUtil.assertTrue("must have an id", optElement.get().hasId());
				}

				return optElement;
			}
		});


	}

	protected abstract Optional<MetaElement> doAllocateMetaElement(Type type);

	protected Optional<MetaElement> allocateEnumType(Type type) {
		if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			if (clazz.isEnum()) {
				MetaEnumType enumType = new MetaEnumType();
				enumType.setElementType(enumType);
				enumType.setImplementationType(type);
				enumType.setName(clazz.getSimpleName());
				for (Object literalObj : clazz.getEnumConstants()) {
					MetaLiteral literal = new MetaLiteral();
					literal.setName(literalObj.toString());
					literal.setParent(enumType, true);
				}
				return addElement(type, enumType);
			}
		}
		return Optional.empty();
	}

	private Optional<MetaElement> allocateCollectionType(Type type) {
		if (type instanceof Class && ((Class) type).isArray()) {
			Class<?> elementClass = ((Class<?>) type).getComponentType();
			Optional<MetaElement> elementType = allocateMetaElement(elementClass);
			if (elementType.isPresent()) {
				MetaArrayType arrayType = new MetaArrayType();
				arrayType.setName(elementType.get().getName() + "$array");
				arrayType.setId(elementType.get().getId() + "$array");
				arrayType.setImplementationType(type);
				arrayType.setElementType((MetaType) elementType.get());
				return addElement(type, arrayType);
			}
		}

		if (type instanceof ParameterizedType && ((ParameterizedType) type).getActualTypeArguments().length == 1) {
			ParameterizedType paramType = (ParameterizedType) type;
			boolean isSet = Set.class.isAssignableFrom((Class<?>) paramType.getRawType());
			boolean isList = List.class.isAssignableFrom((Class<?>) paramType.getRawType());

			if (!isSet && !isList) {
				return Optional.empty();
			}

			// Only look at the type parameter if a Set or List was detected.
			Optional<MetaType> elementType = (Optional) allocateMetaElement(paramType.getActualTypeArguments()[0]);
			if (!elementType.isPresent()) {
				return Optional.empty();
			}

			if (isSet) {
				MetaSetType metaSet = new MetaSetType();
				metaSet.setId(elementType.get().getId() + "$set");
				metaSet.setName(elementType.get().getName() + "$set");
				metaSet.setImplementationType(paramType);
				metaSet.setElementType(elementType.get());
				return addElement(type, metaSet);
			} else {
				PreconditionUtil.assertTrue("expected a list type", isList);
				MetaListType metaList = new MetaListType();
				metaList.setId(elementType.get().getId() + "list");
				metaList.setName(elementType.get().getName() + "$list");
				metaList.setImplementationType(paramType);
				metaList.setElementType(elementType.get());
				return addElement(type, metaList);
			}
		}
		return Optional.empty();
	}

	private Optional<MetaElement> allocateMap(ParameterizedType paramType) {
		if (paramType.getRawType() instanceof Class && Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
			PreconditionUtil.assertEquals("expected 2 type arguments", 2, paramType.getActualTypeArguments().length);

			Optional<MetaType> optKeyType = (Optional) allocateMetaElement(paramType.getActualTypeArguments()[0]);
			Optional<MetaType> optValueType = (Optional) allocateMetaElement(paramType.getActualTypeArguments()[1]);
			if (optKeyType.isPresent() && optValueType.isPresent()) {
				MetaType keyType = optKeyType.get();
				MetaType valueType = optValueType.get();
				MetaMapType mapMeta = new MetaMapType();

				boolean primitiveKey = keyType instanceof MetaPrimitiveType;
				boolean primitiveValue = valueType instanceof MetaPrimitiveType;
				if (primitiveKey || !primitiveValue) {
					mapMeta.setName(valueType.getName() + "$mappedBy$" + keyType.getName());
					mapMeta.setId(valueType.getId() + "$mappedBy$" + keyType.getName());
				} else {
					mapMeta.setName(keyType.getName() + "$map$" + valueType.getName());
					mapMeta.setId(keyType.getId() + "$map$" + valueType.getName());
				}

				mapMeta.setImplementationType(paramType);
				mapMeta.setKeyType(keyType);
				mapMeta.setElementType(valueType);
				return addElement(paramType, mapMeta);
			}
		}
		return Optional.empty();
	}

}
