package io.crnk.gen.typescript.transform;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.gen.typescript.model.TSAny;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSObjectType;
import io.crnk.gen.typescript.model.TSPrimitiveType;
import io.crnk.gen.typescript.model.TSType;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSMetaPrimitiveTypeTransformation implements TSMetaTransformation {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSMetaDataObjectTransformation.class);

	private HashMap<Class<?>, TSType> primitiveMapping;

	public TSMetaPrimitiveTypeTransformation() {
		primitiveMapping = new HashMap<>();

		primitiveMapping.put(ObjectNode.class, TSAny.INSTANCE);
		primitiveMapping.put(ArrayNode.class, TSAny.INSTANCE);
		primitiveMapping.put(JsonNode.class, TSAny.INSTANCE);

		primitiveMapping.put(Object.class, TSAny.INSTANCE);
		primitiveMapping.put(String.class, TSPrimitiveType.STRING);
		primitiveMapping.put(Boolean.class, TSPrimitiveType.BOOLEAN);
		primitiveMapping.put(boolean.class, TSPrimitiveType.BOOLEAN);
		primitiveMapping.put(short.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(Short.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(float.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(Float.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(double.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(Double.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(int.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(Integer.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(long.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(Long.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(byte.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(Byte.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(BigDecimal.class, TSPrimitiveType.NUMBER);
		primitiveMapping.put(BigInteger.class, TSPrimitiveType.NUMBER);

		primitiveMapping.put(LocalDate.class, TSPrimitiveType.STRING);
		primitiveMapping.put(LocalDate.class, TSPrimitiveType.STRING);
		primitiveMapping.put(LocalDateTime.class, TSPrimitiveType.STRING);
		primitiveMapping.put(LocalDateTime.class, TSPrimitiveType.STRING);
		primitiveMapping.put(OffsetDateTime.class, TSPrimitiveType.STRING);
		primitiveMapping.put(OffsetDateTime.class, TSPrimitiveType.STRING);
		primitiveMapping.put(UUID.class, TSPrimitiveType.STRING);
		primitiveMapping.put(Duration.class, TSPrimitiveType.STRING);
		primitiveMapping.put(byte[].class, TSPrimitiveType.STRING);
	}

	@Override
	public void postTransform(TSElement element, TSMetaTransformationContext context) {

	}

	@Override
	public boolean accepts(MetaElement element) {
		return element instanceof MetaPrimitiveType;
	}

	@Override
	public TSElement transform(MetaElement element, TSMetaTransformationContext context, TSMetaTransformationOptions options) {
		Class<?> implClass = ((MetaPrimitiveType) element).getImplementationClass();
		if (primitiveMapping.containsKey(implClass)) {
			return primitiveMapping.get(implClass);
		}
		LOGGER.error("unexpected element: {} of type {}", element, implClass.getName());
		return TSAny.INSTANCE;
	}

	@Override
	public boolean isRoot(MetaElement element) {
		return false;
	}
}
