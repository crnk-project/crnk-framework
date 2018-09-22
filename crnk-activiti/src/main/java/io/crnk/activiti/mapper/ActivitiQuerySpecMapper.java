package io.crnk.activiti.mapper;

import io.crnk.activiti.resource.HistoricProcessInstanceResource;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import org.activiti.engine.query.Query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivitiQuerySpecMapper {

	private ActivitiQuerySpecMapper() {
	}


	public static <T> List<T> find(Query activitiQuery, QuerySpec querySpec, List<FilterSpec> baseFilters) {
		try {
			applyFilterSpec(activitiQuery, querySpec, baseFilters);
			applySortSpec(activitiQuery, querySpec);

			Long limit = querySpec.getLimit();
			if (limit != null) {
				return activitiQuery.listPage((int) querySpec.getOffset(), querySpec.getLimit().intValue());
			} else {
				PreconditionUtil.verifyEquals(Long.valueOf(0L), querySpec.getOffset(), "page offset not supported");
				return activitiQuery.list();
			}
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new BadRequestException(e.getMessage(), e);
		} catch (IllegalStateException e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}


	private static void applyFilterSpec(Query activitiQuery, QuerySpec querySpec, List<FilterSpec> baseFilters)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

		List<FilterSpec> filters = new ArrayList<>();
		filters.addAll(baseFilters);
		filters.addAll(querySpec.getFilters());

		for (FilterSpec filterSpec : filters) {
			List<String> attributePath = filterSpec.getAttributePath();
			if (attributePath.size() == 2 && "id".equals(attributePath.get(1))) {
				attributePath = Arrays.asList(attributePath.get(0) + "Id");
			}
			PreconditionUtil.verify(attributePath.size() == 1, "nested attribute paths not available, path=%s", attributePath);
			String attrName = attributePath.get(0);

			FilterOperator op = filterSpec.getOperator();
			Class valueClass = filterSpec.getValue().getClass();
			String attributeName = mapAttributeName(attrName, querySpec.getResourceClass(), op, valueClass);
			Object value = filterSpec.getValue();

			Method method = getMethod(activitiQuery.getClass(), attributeName);
			if (method.getParameterCount() == 0) {
				PreconditionUtil.verifyEquals(Boolean.TRUE, value,
						"only filtering by true supported for boolean values, attributeName=%s", attributeName);
				method.invoke(activitiQuery);
			} else {
				Class<?> expectedType = method.getParameterTypes()[0];
				method.invoke(activitiQuery, unmapValue(value, expectedType));
			}
		}
	}

	private static Object unmapValue(Object value, Class expectedType) {
		if (value instanceof List && Set.class.isAssignableFrom(expectedType)) {
			return new HashSet<>((Collection) value);
		}

		if (value instanceof OffsetDateTime) {
			return Date.from(((OffsetDateTime) value).toInstant());
		}
		return value;
	}

	private static Method getMethod(Class<? extends Query> clazz, String methodName) {
		for (Method method : clazz.getMethods()) {
			if (methodName.equals(method.getName())) {
				return method;
			}
		}
		throw new BadRequestException("parameter '" + methodName + "' not found");
	}

	private static String mapAttributeName(String attributeName, Class<?> resourceClass, FilterOperator operator,
										   Class valueClass) {
		String name = attributeName;

		boolean many = Collection.class.isAssignableFrom(valueClass);
		boolean isDate = OffsetDateTime.class.isAssignableFrom(valueClass);

		if (operator.equals(FilterOperator.EQ) && many) {
			if (name.toLowerCase().endsWith("id")) {
				name = name + "s";
			} else {
				name = name + "Ids";
			}
		} else if (operator.equals(FilterOperator.LIKE) && !many) {
			name = name + "LikeIgnoreCase";
		} else if (operator.equals(FilterOperator.GT) && isDate) {
			name = mapDateTimeName(name);
			name = name + "After";
		} else if (operator.equals(FilterOperator.LT) && isDate) {
			name = mapDateTimeName(name);
			name = name + "Before";
		} else if (operator.equals(FilterOperator.GE) && !many) {
			name = "min" + firstToUpper(name);
		} else if (operator.equals(FilterOperator.LE) && !many) {
			name = "max" + firstToUpper(name);
		} else if (!operator.equals(FilterOperator.EQ)) {
			throw new BadRequestException("filter operator '" + operator + "' not supported");
		}

		return addTypePrefix(resourceClass, name);
	}

	private static String mapDateTimeName(String name) {
		if (name.endsWith("Date")) {
			return name.substring(0, name.length() - 4);
		}
		if (name.endsWith("startTime")) {
			return "started";
		}
		return name;
	}

	private static String firstToUpper(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private static void applySortSpec(Query activitiQuery, QuerySpec querySpec)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		for (SortSpec orderSpec : querySpec.getSort()) {
			List<String> attributePath = orderSpec.getAttributePath();
			PreconditionUtil.verify(attributePath.size() == 1, "nested attribute paths not supported");
			String attributeName = attributePath.get(0);

			String orderbyMethodName = "orderBy" + firstToUpper(addTypePrefix(querySpec.getResourceClass(), attributeName));
			Method method = activitiQuery.getClass().getMethod(orderbyMethodName);
			method.invoke(activitiQuery);

			if (orderSpec.getDirection() == Direction.DESC) {
				activitiQuery.desc();
			} else {
				activitiQuery.asc();
			}
		}
	}

	private static String addTypePrefix(Class<?> resourceClass, String attributeName) {
		if (!attributeName.startsWith("task") && !attributeName.startsWith("process") && !attributeName.startsWith("started")) {
			if (TaskResource.class.isAssignableFrom(resourceClass)) {
				return "task" + firstToUpper(attributeName);
			}
			if (HistoricProcessInstanceResource.class.isAssignableFrom(resourceClass) || ProcessInstanceResource.class
					.isAssignableFrom(resourceClass)) {
				return "processInstance" + firstToUpper(attributeName);
			}
		}
		return attributeName;
	}
}
