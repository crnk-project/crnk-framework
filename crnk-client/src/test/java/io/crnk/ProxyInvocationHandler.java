//package io.crnk;
//
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//
//import io.crnk.ResourceProxyFactory.ResourceProxy;
//import io.crnk.core.document.field.ResourceField;
//
//public class ProxyInvocationHandler implements InvocationHandler {
//
//
//
//	@Override
//	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//
//		if (method.getDeclaringClass() == Object.class) {
//			return invokeObjectMethod(proxy, method, args);
//		}
//		else if (method.getDeclaringClass() == ResourceProxy.class) {
//			return invokeProxyMethod(proxy, method, args);
//		}
//
//		return invokeBeanMethod(proxy, method, args);
//	}
//
//	private Object invokeBeanMethod(Object proxy, Method method, Object[] args) {
//		String name = getPropertyName(method);
//
//
//
//		System.out.println(method);
//		return null;
//	}
//
//	private String getPropertyName(Method method) {
//		String name = method.getName();
//		if (name.startsWith("is")) {
//			return Character.toLowerCase(name.charAt(2)) + name.substring(2);
//		}
//		else if (name.startsWith("set") || name.startsWith("get") || name.startsWith("has")) {
//			return Character.toLowerCase(name.charAt(3)) + name.substring(3);
//		}
//		throw new IllegalStateException(method.toString());
//	}
//
//	private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
//		if (method.getName().equals("equals")) {
//			Object otherObject = args[0];
//			return otherObject == proxy;
//		}
//		if (method.getName().equals("toString")) {
//			return resourceClass.getName() + "$" + "[" + url + "]";
//		}
//		if (method.getName().equals("hashCode")) {
//			return url.hashCode();
//		}
//		throw new IllegalStateException(method.toString());
//	}
//
//	private Object invokeProxyMethod(Object proxy, Method method, Object[] args) {
//					if (method.getName().equals("getUrl")) {
//						return url;
//					}
//					if (method.getName().equals("getUrl")) {
//						return url;
//					}
//					throw new IllegalStateException(method.toString());
//				}
//}}
