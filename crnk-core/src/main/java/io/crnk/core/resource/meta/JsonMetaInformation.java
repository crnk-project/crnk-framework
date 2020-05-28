package io.crnk.core.resource.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.crnk.core.engine.internal.utils.CastableInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JsonMetaInformation implements MetaInformation, CastableInformation<MetaInformation> {

	private JsonNode data;

	private ObjectMapper mapper;

	public JsonMetaInformation(JsonNode data, ObjectMapper mapper) {
		this.data = data;
		this.mapper = mapper;
	}

	public JsonNode asJsonNode() {
		return data;
	}

	/**
	 * Converts this generic meta information to the provided type.
	 *
	 * @param metaClass to return
	 * @return meta information based on the provided type.
	 */
	@Override
	public <M extends MetaInformation> M as(Class<M> metaClass) {
		try {
			if (metaClass.isInterface()) {
				return createInterfaceJsonAdapter(metaClass, data, mapper);
			}
			return mapper.readerFor(metaClass).readValue(data);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	protected static <T> T 	createInterfaceJsonAdapter(Class<T> interfaceClass, JsonNode data, final ObjectMapper mapper) {
		Class[] interfaces = new Class[] { interfaceClass };
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getDeclaringClass().equals(Object.class)) {
					throw new UnsupportedOperationException("not implemented");
				}

				String name = ClassUtils.getGetterFieldName(method);
				JsonNode jsonNode = data.get(name);
				if(jsonNode != null) {
					Class<?> returnType = method.getReturnType();
					ObjectReader objectReader = mapper.readerFor(returnType);
					return objectReader.readValue(jsonNode);
				}
				return null;
			}
		});
	}
}
