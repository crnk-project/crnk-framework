package io.crnk.meta.internal;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.provider.MetaPartitionBase;

public class BaseMetaPartition extends MetaPartitionBase {

	private static final String BASE_ID_PREFIX = "base.";

	private Set<Class<?>> primitiveTypes = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());


	public BaseMetaPartition() {
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
		registerPrimitiveType(ObjectNode.class);
		registerPrimitiveType(ArrayNode.class);
		registerPrimitiveType(FilterOperator.class);
		registerPrimitiveType(PathSpec.class);
		registerPrimitiveType(byte[].class);
		registerPrimitiveType(boolean[].class);
		registerPrimitiveType(int[].class);
		registerPrimitiveType(short[].class);
		registerPrimitiveType(long[].class);
		registerPrimitiveType(double[].class);
		registerPrimitiveType(float[].class);
	}

	@Override
	public MetaElement getMeta(Type type) {
		if (type instanceof Class) {
			return super.getMeta(mapPrimitiveType((Class) type));
		}
		return super.getMeta(type);
	}

	protected Optional<MetaElement> addElement(Type type, MetaElement element) {
		if (type instanceof Class) {
			Class<?> objectType = mapPrimitiveType((Class) type);
			return super.addElement(objectType, element);
		}
		else {
			return super.addElement(type, element);
		}
	}

	protected Optional<MetaElement> doAllocateMetaElement(Type type) {
		if (type instanceof Class) {
			Class clazz = (Class) type;
			clazz = mapPrimitiveType(clazz);

			if (isPrimitiveType(clazz)) {
				String id = BASE_ID_PREFIX + firstToLower(clazz.getSimpleName());

				Optional<MetaElement> optPrimitiveType = context.getMetaElement(id);
				if (!optPrimitiveType.isPresent()) {
					MetaPrimitiveType primitiveType = new MetaPrimitiveType();
					primitiveType.setElementType(primitiveType);
					primitiveType.setImplementationType(clazz);
					primitiveType.setName(firstToLower(clazz.getSimpleName()));
					primitiveType.setId(id);
					return addElement(type, primitiveType);
				}
				return optPrimitiveType;
			}
		}
		return Optional.empty();
	}

	@Override
	protected Optional<MetaElement> allocateEnumType(Type type) {
		// enums not part of base
		return Optional.empty();
	}

	public void registerPrimitiveType(Class<?> clazz) {
		primitiveTypes.add(clazz);
	}

	private static String firstToLower(String name) {
		if (name.equals(JsonNode.class.getSimpleName())) {
			return "json";
		}
		if (name.equals(ObjectNode.class.getSimpleName())) {
			return "json.object";
		}
		if (name.equals(ArrayNode.class.getSimpleName())) {
			return "json.array";
		}
		if (name.equals("UUID")) {
			return "uuid";
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
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
			return Boolean.class;
		}
		return clazz;
	}

	private boolean isPrimitiveType(Class<?> clazz) {
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

	@Override
	public void discoverElements() {

	}
}
