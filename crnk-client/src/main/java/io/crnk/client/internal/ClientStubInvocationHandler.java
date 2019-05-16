package io.crnk.client.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceListBase;
import net.jodah.typetools.TypeResolver;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ClientStubInvocationHandler implements InvocationHandler {

	private ResourceRepository<?, Serializable> repositoryStub;

	private Object actionStub;

	private Map<Method, Method> interfaceStubMethodMap = new HashMap<>();

	public ClientStubInvocationHandler(Class<?> repositoryInterface,
                                       ResourceRepository<?, Serializable> repositoryStub, Object actionStub) {
		this.repositoryStub = repositoryStub;
		this.actionStub = actionStub;
		setupRepositoryMethods(repositoryInterface);
	}

	private static String getMethodId(Method method) {
		StringBuilder builder = new StringBuilder();
		builder.append(method.getName());
		for (Class<?> paramType : method.getParameterTypes()) {
			builder.append("#");
			builder.append(paramType.getName());
		}
		return builder.toString();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (method.getDeclaringClass().isAssignableFrom(ResourceRepository.class)) {
				// execute document method
				return method.invoke(repositoryStub, args);
			} else if (interfaceStubMethodMap.containsKey(method)) {
				return invokeInterfaceMethod(method, args);
			} else {
				PreconditionUtil.verify(actionStub != null,
						"cannot execute non-JSONAPI method, call CrnkClient.setActionStubFactory(...) first, e.g. with "
								+ "JerseyActionStubFactory for JAX-RS",
						actionStub);

				// execute action
				return method.invoke(actionStub, args);
			}
		} catch (InvocationTargetException e) { // NOSONAR ok this way
			throw e.getCause();
		}
	}

	private Object invokeInterfaceMethod(Method method, Object[] args)
			throws IllegalAccessException, InvocationTargetException {
		Method stubMethod = interfaceStubMethodMap.get(method);
		Object result = stubMethod.invoke(repositoryStub, args);

		Class<?> returnType = method.getReturnType();
		if (result == null || returnType.isInstance(result)) {
			return result;
		} else if (result instanceof DefaultResourceList) {
			return createTypesafeList(result, returnType);
		} else {
			throw new IllegalStateException("cannot cast return type " + result + " to " + returnType.getName());
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private Object createTypesafeList(Object result, Class<?> returnType) {
		DefaultResourceList defaultList = (DefaultResourceList) result;

		Class<?>[] typeArguments = TypeResolver.resolveRawArguments(ResourceListBase.class, returnType);
		Class<?> metaType = typeArguments[1];
		Class<?> linksType = typeArguments[2];

		ResourceListBase typedList = (ResourceListBase) ClassUtils.newInstance(returnType);
		typedList.addAll(defaultList);
		typedList.setMeta(defaultList.getMeta(metaType));
		typedList.setLinks(defaultList.getLinks(linksType));
		return typedList;
	}

	private void setupRepositoryMethods(Class<?> repositoryInterface) {
		Map<String, Method> stubMethods = new HashMap<>();
		setupRepositoryMethods(stubMethods, repositoryInterface);

	}

	private void setupRepositoryMethods(Map<String, Method> stubMethods, Class<?> repositoryInterface) {
		for (Method method : ResourceRepository.class.getMethods()) {
			stubMethods.put(getMethodId(method), method);
		}

		for (Method method : repositoryInterface.getDeclaredMethods()) {
			String id = getMethodId(method);
			Method stubMethod = stubMethods.get(id);
			if (stubMethod != null) {
				interfaceStubMethodMap.put(method, stubMethod);
			}
		}

		for (Class<?> superInterface : repositoryInterface.getInterfaces()) {
			setupRepositoryMethods(stubMethods, superInterface);
		}
	}
}
