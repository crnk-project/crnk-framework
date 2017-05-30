package io.crnk.validation.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path.Node;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.ExceptionMapperHelper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	protected static final String META_RESOURCE_ID = "resourceId";

	protected static final String META_RESOURCE_TYPE = "resourceType";

	protected static final String META_TYPE_KEY = "type";

	protected static final String META_TYPE_VALUE = "ConstraintViolation";

	protected static final String META_MESSAGE_TEMPLATE = "messageTemplate";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintViolationExceptionMapper.class);

	private static final String HIBERNATE_PROPERTY_NODE_IMPL = "org.hibernate.validator.path.PropertyNode";

	private static final Object HIBERNATE_PROPERTY_NODE_ENGINE_IMPL = "org.hibernate.validator.internal.engine.path.NodeImpl";

	private static final String DEFAULT_PRIMARY_KEY_NAME = "id";

	private ModuleContext context;

	public ConstraintViolationExceptionMapper(ModuleContext context) {
		this.context = context;
	}

	private static Object getValue(Node propertyNode) {
		// bean validation not sufficient for sets
		// not possible to access elements, reverting to
		// Hibernate implementation
		// TODO investigate other implementation next to
		// hibernate, JSR 303 v1.1 not sufficient

		boolean hibernateNodeImpl =
				propertyNode.getClass().getName().equals(HIBERNATE_PROPERTY_NODE_IMPL); // NOSONAR class / may not be available
		boolean hiberanteNodeImpl2 = propertyNode.getClass().getName().equals(HIBERNATE_PROPERTY_NODE_ENGINE_IMPL); // NOSONAR;
		if (hibernateNodeImpl || hiberanteNodeImpl2) {
			try {
				Method parentMethod = propertyNode.getClass().getMethod("getParent");
				Method valueMethod = propertyNode.getClass().getMethod("getValue");
				Object parentNode = parentMethod.invoke(propertyNode);
				if (parentNode != null) {
					return valueMethod.invoke(parentNode);
				}
				else {
					return valueMethod.invoke(propertyNode);
				}
			}
			catch (Exception e) {
				throw new UnsupportedOperationException(e);
			}
		}
		else {
			throw new UnsupportedOperationException(
					"cannot convert violations for java.util.Set elements, consider using Hibernate validator");
		}
	}

	private static Object getParameterValue(Node propertyNode) {
		// bean validation not sufficient for sets
		// not possible to access elements, reverting to
		// Hibernate implementation
		// TODO investigate other implementation next to
		// hibernate, JSR 303 v1.1 not sufficient
		if (propertyNode.getClass().getName().equals(HIBERNATE_PROPERTY_NODE_IMPL)
				|| propertyNode.getClass().getName().equals(HIBERNATE_PROPERTY_NODE_ENGINE_IMPL)) { // NOSONAR
			try {
				Method valueMethod = propertyNode.getClass().getMethod("getValue");
				return valueMethod.invoke(propertyNode);
			}
			catch (Exception e) {
				throw new UnsupportedOperationException(e);
			}
		}
		else {
			throw new UnsupportedOperationException(
					"cannot convert violations for java.util.Set elements, consider using Hibernate validator");
		}
	}

	@Override
	public ErrorResponse toErrorResponse(ConstraintViolationException cve) {
		LOGGER.warn("a ConstraintViolationException occured", cve);

		List<ErrorData> errors = new ArrayList<>();
		for (ConstraintViolation<?> violation : cve.getConstraintViolations()) {

			ErrorDataBuilder builder = ErrorData.builder();
			builder = builder.addMetaField(META_TYPE_KEY, META_TYPE_VALUE);
			builder = builder.setStatus(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY_422));
			builder = builder.setTitle(violation.getMessage());

			builder = builder.setCode(toCode(violation));
			if (violation.getMessageTemplate() != null) {
				builder = builder.addMetaField(META_MESSAGE_TEMPLATE, violation.getMessageTemplate());
			}

			// for now we just provide root resource validation information
			// depending on bulk update spec, we might also provide the leaf information in the future
			if (violation.getRootBean() != null) {
				ResourceRef resourceRef = resolvePath(violation);
				builder = builder.addMetaField(META_RESOURCE_ID, resourceRef.getRootResourceId());
				builder = builder.addMetaField(META_RESOURCE_TYPE, resourceRef.getRootResourceType());
				builder = builder.setSourcePointer(resourceRef.getRootSourcePointer());
			}

			ErrorData error = builder.build();
			errors.add(error);
		}

		return ErrorResponse.builder().setStatus(HttpStatus.UNPROCESSABLE_ENTITY_422).setErrorData(errors).build();
	}

	private String toCode(ConstraintViolation<?> violation) {
		if (violation.getConstraintDescriptor() != null) {
			Annotation annotation = violation.getConstraintDescriptor().getAnnotation();

			if (annotation != null) {
				Class<?> clazz = annotation.getClass();
				Class<?> superclass = annotation.getClass().getSuperclass();
				Class<?>[] interfaces = annotation.getClass().getInterfaces();
				if (superclass == Proxy.class && interfaces.length == 1) {
					clazz = interfaces[0];
				}

				return clazz.getName();
			}
		}
		if (violation.getMessageTemplate() != null) {
			return violation.getMessageTemplate().replace("{", "").replaceAll("}", "");
		}
		return null;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public ConstraintViolationException fromErrorResponse(ErrorResponse errorResponse) {
		Set violations = new HashSet();

		Iterable<ErrorData> errors = errorResponse.getErrors();
		for (ErrorData error : errors) {
			ConstraintViolationImpl violation = ConstraintViolationImpl.fromError(context.getResourceRegistry(), error);
			violations.add(violation);
		}

		return new ConstraintViolationException(null, violations);
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return ExceptionMapperHelper.accepts(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY_422, META_TYPE_VALUE);
	}

	/**
	 * Translate validated bean and root path into validated resource and
	 * resource path. For example, embeddables belonging to an entity document
	 * are mapped back to an entity violation and a proper path to the
	 * embeddable attribute.
	 *
	 * @param violation to compute the reference
	 * @return computaed reference
	 */
	private ResourceRef resolvePath(ConstraintViolation<?> violation) {
		Object resource = violation.getRootBean();

		Object nodeObject = resource;
		ResourceRef ref = new ResourceRef(resource);

		Iterator<Node> iterator = violation.getPropertyPath().iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();

			// ignore methods/parameters
			if (node.getKind() == ElementKind.METHOD) {
				continue;
			}
			if (node.getKind() == ElementKind.PARAMETER) {
				resource = getParameterValue(node);
				nodeObject = resource;
				ref = new ResourceRef(resource);
				assertResource(resource);
				continue;
			}

			// visit list, set, map references
			nodeObject = ref.getNodeReference(nodeObject, node);
			ref.visitNode(nodeObject);

			// visit property
			nodeObject = ref.visitProperty(nodeObject, node);
		}

		return ref;
	}

	private void assertResource(Object resource) {
		if (!isResource(resource.getClass())) {
			throw new IllegalStateException("a resource must be used as root, got " + resource + " instead");
		}
	}

	private boolean isResource(Class<?> clazz) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		return resourceRegistry.hasEntry(clazz);
	}

	/**
	 * @param resource to get the id from
	 * @return id of the given resource
	 */
	protected String getResourceId(Object resource) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry entry = resourceRegistry.findEntry(resource.getClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		ResourceField idField = resourceInformation.getIdField();
		Object id = idField.getAccessor().getValue(resource);
		if (id != null) {
			return id.toString();
		}
		return null;
	}

	protected String getResourceType(Object resource) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry entry = resourceRegistry.findEntry(resource.getClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		return resourceInformation.getResourceType();
	}

	class ResourceRef {

		private Object rootResource;

		private Object leafResource;

		private StringBuilder rootSourcePointer = new StringBuilder();

		private StringBuilder leafSourcePointer = new StringBuilder();

		public ResourceRef(Object resource) {
			this.leafResource = resource;
			this.rootResource = resource;
		}

		public String getRootSourcePointer() {
			return rootSourcePointer.toString();
		}

		public Object visitProperty(Object nodeObject, Node node) {
			Object next;
			if (node.getKind() == ElementKind.PROPERTY) {
				next = PropertyUtils.getProperty(nodeObject, node.getName());
			}
			else if (node.getKind() == ElementKind.BEAN) {
				next = nodeObject;
			}
			else {
				throw new UnsupportedOperationException("unknown node: " + node);
			}

			if (node.getName() != null) {
				appendSeparator();
				if (!isResource(nodeObject.getClass()) || isPrimaryKey(nodeObject.getClass(), node.getName())) {
					// continue along attributes path or primary key on root
					appendSourcePointer(node.getName());
				}
				else if (isAssociation(nodeObject.getClass(), node.getName())) {
					appendSourcePointer("data/relationships/");
					appendSourcePointer(node.getName());
				}
				else {

					appendSourcePointer("data/attributes/");
					appendSourcePointer(node.getName());
				}
			}
			return next;
		}

		private Object getNodeReference(Object element, Node node) {
			Integer index = node.getIndex();
			Object key = node.getKey();
			if (index != null) {
				appendSeparator();
				appendSourcePointer(index);
				return ((List<?>) element).get(index);
			}
			else if (key != null) {
				appendSeparator();
				appendSourcePointer(key);
				return ((Map<?, ?>) element).get(key);
			}
			else if (element instanceof Set && getValue(node) != null) {
				Object elementEntry = getValue(node);

				// since sets get translated to arrays, we do the same here
				// crnk-client allocates sets that preserver the order
				// of arrays
				List<Object> list = new ArrayList<>();
				list.addAll((Set<?>) element);
				index = list.indexOf(elementEntry);

				appendSeparator();
				appendSourcePointer(index);

				return getValue(node);
			}
			return element;
		}

		private void appendSourcePointer(Object object) {
			leafSourcePointer.append(object);
			if (!withinRelation()) {
				// bulk update of resources not support by json api spec,
				//so we stop for sourcePointer computation when a relation
				// could not be validated. How to continue depends on future implementations
				rootSourcePointer.append(object);
			}
		}

		private boolean withinRelation() {
			return rootResource != leafResource;
		}

		private void appendSeparator() {
			if (leafSourcePointer.length() > 0) {
				appendSourcePointer("/");
			}
		}

		private boolean isPrimaryKey(Class<? extends Object> clazz, String name) {
			ResourceRegistry resourceRegistry = context.getResourceRegistry();
			RegistryEntry entry = resourceRegistry.findEntry(clazz);
			if (entry != null) {
				ResourceInformation resourceInformation = entry.getResourceInformation();
				ResourceField idField = resourceInformation.getIdField();
				return idField.getUnderlyingName().equals(name);
			}
			else {
				return DEFAULT_PRIMARY_KEY_NAME.equals(name);
			}
		}

		private boolean isAssociation(Class<? extends Object> clazz, String name) {
			ResourceRegistry resourceRegistry = context.getResourceRegistry();
			RegistryEntry entry = resourceRegistry.findEntry(clazz);
			if (entry != null) {
				ResourceInformation resourceInformation = entry.getResourceInformation();
				ResourceField relationshipField = resourceInformation.findRelationshipFieldByName(name);
				return relationshipField != null;
			}
			else {
				return false;
			}
		}

		public void visitNode(Object nodeValue) {
			boolean isResource = nodeValue != null && isResource(nodeValue.getClass());
			if (isResource) {
				leafSourcePointer = new StringBuilder();
				leafResource = nodeValue;
			}
		}

		public Object getRootResourceId() {
			return ConstraintViolationExceptionMapper.this.getResourceId(rootResource);
		}

		public Object getRootResourceType() {
			return ConstraintViolationExceptionMapper.this.getResourceType(rootResource);
		}

		/**
		 * Leaf resource being validated.
		 */
		public Object getLeafResource() {
			return leafResource;
		}

		public Object getRootResource() {
			return rootResource;
		}
	}
}
