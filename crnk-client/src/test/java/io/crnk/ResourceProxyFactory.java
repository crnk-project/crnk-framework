//package io.crnk;
//
//import java.lang.reflect.Method;
//import java.util.List;
//
//import io.crnk.test.mock.models.Task;
//import io.crnk.core.document.field.ResourceFieldNameTransformer;
//import io.crnk.core.document.information.AnnotationResourceInformationBuilder;
//import io.crnk.core.engine.information.document.ResourceInformation;
//import nl.jqno.equalsverifier.internal.cglib.proxy.Enhancer;
//
//public class ResourceProxyFactory {
//
//	public static void main(String[] args) {
//		ResourceProxyFactory factory = new ResourceProxyFactory();
//
//		AnnotationResourceInformationBuilder informationBuilder = new AnnotationResourceInformationBuilder(
//				new ResourceFieldNameTransformer());
//		ResourceInformation information = informationBuilder.build(Task.class);
//
//		Task task = factory.createResourceProxy(Task.class, information, 12, "http://");
//
//		System.out.println(task);
//
//		ResourceProxy proxy = (ResourceProxy) task;
//		System.out.println(proxy.getResourceUrl());
//
//	}
//
//	public interface ResourceProxy {
//
//		public Object getResourceId();
//
//		public String getResourceUrl();
//
//		public Object getResourceObject();
//
//		public void setResourceId(Object id);
//
//		public void setResourceUrl(String url);
//
//		public void setResourceObject(Object object);
//	}
//
//	@SuppressWarnings("unchecked")
//	public <T> T createResourceProxy(final Class<T> resourceClass, final ResourceInformation information, final Object id,
//			final String url) {
//
//		Enhancer enhancer = new Enhancer();
//		enhancer.setSuperclass(resourceClass);
//
//		enhancer.setInterfaces(new Class[] { ResourceProxy.class });
//
////		enhancer.setCallbackFilter(filter);
////
////		enhancer.setCallback(new MethodInterceptor() {
////
////			@Override
////			public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
////				// TODO Auto-generated method stub
////				return null;
////			}
////		});
//
//		//Class<T> dynamicType = enhancer.createClass();
//
//		//		ByteBuddy byteBuddy = new ByteBuddy();
//		//		Builder<T> subclass = byteBuddy.subclass(resourceClass);
//		//
//		//		subclass = subclass.implement(ResourceProxy.class);
//		//		subclass = subclass.name(resourceClass.getName() + "$");
//		//
//		//		String idName = information.getIdField().getUnderlyingName();
//		//		Junction<MethodDescription> and = ElementMatchers.any()
//		//				.and(ElementMatchers.not(ElementMatchers.isDeclaredByGeneric(ResourceProxy.class)))
//		//				.and(ElementMatchers.not(ElementMatchers.isGetter(idName)));
//		//		ImplementationDefinition<T> anyMethod = subclass.method(and);
//		//
//		//		subclass = anyMethod.intercept(InvocationHandlerAdapter.of(new ProxyInvocationHandler()));
//		//
//		//		subclass = subclass.defineField("$resourceObject", Object.class, 0);
//		//		subclass = subclass.defineField("$resourceUrl", String.class, 0);
//		//		subclass = subclass.defineField("$resourceId", Object.class, 0);
//		//		subclass = subclass.method(ElementMatchers.named("getResourceId")).intercept(FieldAccessor.ofField("$resourceId"));
//		//		subclass = subclass.method(ElementMatchers.named("setResourceId")).intercept(FieldAccessor.ofField("$resourceId"));
//		//		subclass = subclass.method(ElementMatchers.named("getResourceUrl")).intercept(FieldAccessor.ofField("$resourceUrl"));
//		//		subclass = subclass.method(ElementMatchers.named("setResourceUrl")).intercept(FieldAccessor.ofField("$resourceUrl"));
//		//		subclass = subclass.method(ElementMatchers.named("getResourcObject")).intercept(FieldAccessor.ofField("$resourceObject"));
//		//		subclass = subclass.method(ElementMatchers.isGetter(idName)).intercept(FieldAccessor.ofField("$resourceId"));
//		//Class<T> dynamicType = (Class<T>) subclass.make().load(getClass().getClassLoader()).getLoaded();
//
//		try {
//			T document = dynamicType.newInstance();
//			ResourceProxy proxy = (ResourceProxy) document;
//			proxy.setResourceId(id);
//			proxy.setResourceUrl(url);
//			return document;
//		}
//		catch (InstantiationException | IllegalAccessException e) {
//			throw new IllegalStateException(e);
//		}
//	}
//
//	public <T> List<T> createListProxy(Class<T> resourceClass, String url) {
//		return null;
//	}
//
//}
