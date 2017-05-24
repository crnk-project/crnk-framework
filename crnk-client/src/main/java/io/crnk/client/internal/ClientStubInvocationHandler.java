package io.crnk.client.internal;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.crnk.client.CrnkClient;
import io.crnk.client.action.ActionStubFactory;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceListBase;
import net.jodah.typetools.TypeResolver;

public class ClientStubInvocationHandler implements InvocationHandler {

	private ResourceRepositoryV2<?, Serializable> repositoryStub;

	private Object actionStub;

	private Map<Method, Method> interfaceStubMethodMap = new HashMap<>();

	public ClientStubInvocationHandler(Class<?> repositoryInterface,
									   ResourceRepositoryV2<?, Serializable> repositoryStub, Object actionStub) {
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
			if (method.getDeclaringClass().isAssignableFrom(ResourceRepositoryV2.class)) {
				// execute document method
				return method.invoke(repositoryStub, args);
			} else if (interfaceStubMethodMap.containsKey(method)) {
				return invokeInterfaceMethod(method, args);
			} else if (actionStub != null) {
				// execute action
				return method.invoke(actionStub, args);
			} else {
				throw new IllegalStateException("cannot execute actions, no " + ActionStubFactory.class.getSimpleName()
						+ " set with " + CrnkClient.class.getName());
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
		for (Method method : ResourceRepositoryV2.class.getMethods()) {
			stubMethods.put(getMethodId(method), method);
		}

		for (Method method : repositoryInterface.getDeclaredMethods()) {
			String id = getMethodId(method);
			Method stubMethod = stubMethods.get(id);
			if (stubMethod != null) {
				interfaceStubMethodMap.put(method, stubMethod);
			}
		}
	}
}
