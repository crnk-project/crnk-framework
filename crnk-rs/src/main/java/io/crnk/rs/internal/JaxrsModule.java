package io.crnk.rs.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.SecurityContext;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryAction.RepositoryActionType;
import io.crnk.core.module.Module;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationProvider;

public class JaxrsModule implements Module {

	private static final String ID_ACTION_PARAMETER = "{id}";

	private SecurityContext securityContext;

	public JaxrsModule(SecurityContext securityContext) {
		this.securityContext = securityContext;
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepositoryInformationBuilder(new JaxrsResourceRepositoryInformationProvider());
		context.addExceptionMapper(new WebApplicationExceptionMapper());

		if (securityContext != null) {
			context.addSecurityProvider(new JaxrsSecurityProvider(securityContext));
		}
	}

	@Override
	public String getModuleName() {
		return "jaxrs";
	}

	public static class JaxrsResourceRepositoryInformationProvider extends DefaultResourceRepositoryInformationProvider {

		@Override
		protected Map<String, RepositoryAction> buildActions(Class<? extends Object> repositoryClass) {
			HashMap<String, RepositoryAction> actions = new HashMap<>();

			// search for annotated methods on classes and interfaces
			// since annotations are no inherited
			setupClass(actions, repositoryClass);
			for (Class<?> interfaceClass : repositoryClass.getInterfaces()) {
				setupClass(actions, interfaceClass);
			}
			return actions;
		}

		private void setupClass(HashMap<String, RepositoryAction> actions, Class<? extends Object> repositoryClass) {
			for (Method method : repositoryClass.getMethods()) {
				setupMethod(actions, method);
			}
		}

		private void setupMethod(HashMap<String, RepositoryAction> actions, Method method) {
			Path pathAnnotation = method.getAnnotation(Path.class);
			boolean isJaxRs = isJaxRsMethod(method);
			if (pathAnnotation != null) {
				String path = normPath(pathAnnotation.value());
				String[] pathElements = path.split("\\/");

				checkPathElements(method, pathElements);

				RepositoryActionType actionType = pathElements[0].equals(ID_ACTION_PARAMETER) ? RepositoryActionType.RESOURCE
						: RepositoryActionType.REPOSITORY;

				String name = pathElements[pathElements.length - 1];
				RepositoryAction action = new JaxrsRepositoryAction(name, actionType);
				actions.put(name, action);
			}
			else if (isJaxRs) {
				throw new IllegalStateException("JAXRS actions must be annotated with @Path: " + method);
			}
		}

		/**
		 * There are some strict roles to follow to be a valid action
		 *
		 * @param method holding the @Path annotation
		 * @param pathElements of this method
		 */
		private void checkPathElements(Method method, String[] pathElements) {
			if (pathElements.length == 0 || pathElements.length == 1 && pathElements[0].isEmpty()) {
				throw new IllegalStateException("@Path value must not be empty: " + method);
			}
			if (pathElements.length > 2) {
				throw new IllegalStateException("@Path value must not contain more than two elements: " + method);
			}

			if (pathElements.length == 1 && pathElements[0].equals(ID_ACTION_PARAMETER)) {
				throw new IllegalStateException("single element in @Path cannot be {id}, add action name: " + method);
			}
			if (pathElements.length == 2 && !pathElements[0].equals(ID_ACTION_PARAMETER)) {
				throw new IllegalStateException(
						"for two elements in @Path the first one must be {id}, the second the action name: " + method);
			}
		}

		private boolean isJaxRsMethod(Method method) {
			Path pathAnnotation = method.getAnnotation(Path.class);
			boolean isGet = method.getAnnotation(GET.class) != null;
			boolean isPost = method.getAnnotation(POST.class) != null;
			boolean isPut = method.getAnnotation(PUT.class) != null;
			boolean isDelete = method.getAnnotation(DELETE.class) != null;

			if (isGet || isPost || isPut || isDelete) {
				return true;
			}
			if (pathAnnotation != null) {
				return true;
			}
			return hasJaxRsMethodParameters(method);
		}

		private boolean hasJaxRsMethodParameters(Method method) {
			Annotation[][] parameterAnnotationsArray = method.getParameterAnnotations();
			for (int paramIndex = 0; paramIndex < parameterAnnotationsArray.length; paramIndex++) {
				Annotation[] parameterAnnotations = parameterAnnotationsArray[paramIndex];
				for (Annotation parameterAnnotation : parameterAnnotations) {
					if (parameterAnnotation instanceof PathParam || parameterAnnotation instanceof QueryParam) {
						return true;
					}
				}
			}
			return false;
		}

		private String normPath(String path) {
			String normPath = path;
			if (normPath.startsWith("/")) {
				normPath = normPath.substring(1);
			}
			if (normPath.endsWith("/")) {
				normPath = normPath.substring(0, normPath.length() - 1);
			}
			return normPath;
		}
	}

	public static class JaxrsRepositoryAction implements RepositoryAction {

		private String name;

		private RepositoryActionType actionType;

		public JaxrsRepositoryAction(String name, RepositoryActionType actionType) {
			this.name = name;
			this.actionType = actionType;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public RepositoryActionType getActionType() {
			return actionType;
		}
	}
}
