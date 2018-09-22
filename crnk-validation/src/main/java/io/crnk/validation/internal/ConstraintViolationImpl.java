package io.crnk.validation.internal;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;

import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.metadata.ConstraintDescriptor;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO remo: take care of UnsupportedOperationExceptions to adhere to spec
public class ConstraintViolationImpl implements ConstraintViolation<Object> {

	private ErrorData errorData;

	private Class<?> resourceClass;

	private Serializable resourceId;

	private Path path;

	private ConstraintViolationImpl(ResourceRegistry resourceRegistry, ErrorData errorData) {
		this.errorData = errorData;

		Map<String, Object> meta = this.errorData.getMeta();
		if (meta != null) {
			String strResourceId = (String) meta.get(ConstraintViolationExceptionMapper.META_RESOURCE_ID);
			String resourceType = (String) meta.get(ConstraintViolationExceptionMapper.META_RESOURCE_TYPE);

			if (resourceType != null) {
				RegistryEntry entry = resourceRegistry.getEntry(resourceType);
				resourceClass = entry.getResourceInformation().getResourceClass();
				if (strResourceId != null) {
					resourceId = entry.getResourceInformation().parseIdString(strResourceId);
				}
			}
		}

		String sourcePointer = errorData.getSourcePointer();
		if (sourcePointer != null && resourceClass != null) {
			path = toPath(sourcePointer);
		}
	}

	public static ConstraintViolationImpl fromError(ResourceRegistry resourceRegistry, ErrorData error) {
		return new ConstraintViolationImpl(resourceRegistry, error);
	}

	public ErrorData getErrorData() {
		return errorData;
	}

	private Path toPath(String sourcePointer) { //NOSONAR
		String[] elements = sourcePointer.split("\\/");

		LinkedList<NodeImpl> nodes = new LinkedList<>();

		Type type = resourceClass;

		int i = 0;
		while (i < elements.length) {
			String element = elements[i];
			if (element.isEmpty()) {
				i++;
				continue;
			}

			Class<?> rawType = ClassUtils.getRawType(type);
			if (rawType.equals(List.class) || rawType.equals(Set.class)) {
				// sets handled as list, not quite right, but bean validation spec is not sufficient

				int index = Integer.parseInt(element);
				if (nodes.isEmpty() || nodes.getLast().getIndex() != null) {
					nodes.add(new NodeImpl(null));
				}
				nodes.getLast().index = index;

				// get element type
				PreconditionUtil.assertTrue(type.toString(), type instanceof ParameterizedType);
				ParameterizedType paramType = (ParameterizedType) type;
				type = paramType.getActualTypeArguments()[0];
			} else if (rawType.equals(Map.class)) {
				if (nodes.isEmpty() || nodes.getLast().getKey() != null) {
					nodes.add(new NodeImpl(null));
				}
				nodes.getLast().key = element;

				// get value type
				PreconditionUtil.assertTrue(type.toString(), type instanceof ParameterizedType);
				ParameterizedType paramType = (ParameterizedType) type;
				type = paramType.getActualTypeArguments()[1];
			} else if (isJsonApiStructure(elements, i)) {
				i++; // skip next as well
			} else {
				nodes.add(new NodeImpl(element));

				// follow attribute
				type = PropertyUtils.getPropertyType(rawType, element);
			}
			i++;
		}
		return new PathImpl(nodes);
	}

	private boolean isJsonApiStructure(String[] elements, int i) {
		return "data".equals(elements[i]) && i < elements.length - 2
				&& ("attributes".equals(elements[i + 1]) || "relationships".equals(elements[i + 1]));
	}

	public Serializable getResourceId() {
		return resourceId;
	}

	@Override
	public Object getRootBean() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getLeafBean() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getInvalidValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] getExecutableParameters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getExecutableReturnValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMessage() {
		return errorData.getDetail();
	}

	@Override
	public ConstraintDescriptor<?> getConstraintDescriptor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMessageTemplate() {
		Map<String, Object> meta = this.errorData.getMeta();
		if (meta != null) {
			return (String) meta.get(ConstraintViolationExceptionMapper.META_MESSAGE_TEMPLATE);
		}
		return null;
	}

	@Override
	public Path getPropertyPath() {
		return path;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Class getRootBeanClass() {
		return resourceClass;
	}

	@Override
	public <U> U unwrap(Class<U> arg0) {
		return null;
	}

	class PathImpl implements Path {

		private List<? extends Node> nodes;

		public PathImpl(List<? extends Node> nodes) {
			this.nodes = nodes;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<Node> iterator() {
			return (Iterator<Node>) nodes.iterator();
		}

		@Override
		public String toString() {
			Iterator<Node> iterator = iterator();
			StringBuilder builder = new StringBuilder();
			while (iterator.hasNext()) {
				Node node = iterator.next();
				String name = node.getName();
				if (name != null && builder.length() > 0) {
					builder.append(".");
				}
				builder.append(node);
			}
			return builder.toString();
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Path && obj.toString().equals(toString());
		}
	}

	class NodeImpl implements Node {

		private String name;

		private Integer index;

		private String key;

		public NodeImpl(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isInIterable() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Integer getIndex() {
			return index;
		}

		@Override
		public Object getKey() {
			return key;
		}

		@Override
		public ElementKind getKind() {
			if (path == null) {
				return ElementKind.BEAN;
			} else {
				return ElementKind.PROPERTY;
			}
		}

		@Override
		public <T extends Node> T as(Class<T> nodeType) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (name != null) {
				builder.append(name);
			}
			if (index != null) {
				builder.append("[");
				builder.append(index);
				builder.append("]");
			}
			if (key != null) {
				builder.append("[");
				builder.append(key);
				builder.append("]");
			}
			return builder.toString();
		}
	}

}