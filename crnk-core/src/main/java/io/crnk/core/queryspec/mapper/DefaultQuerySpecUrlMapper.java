package io.crnk.core.queryspec.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.parser.ParserException;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.QuerySpecDeserializerContext;
import io.crnk.core.queryspec.QuerySpecSerializer;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.queryspec.internal.DefaultQueryPathResolver;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultQuerySpecUrlMapper
		implements QuerySpecUrlMapper, QuerySpecDeserializer, QuerySpecSerializer, UnkonwnMappingAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultQuerySpecUrlMapper.class);

	private static final String NULL_VALUE_STRING = "null";

	private FilterOperator defaultOperator = FilterOperator.EQ;

	private Set<FilterOperator> supportedOperators = new HashSet<>();

	private boolean enforceDotPathSeparator = false;

	private boolean ignoreParseExceptions;

	private boolean allowUnknownParameters = false;

	protected QuerySpecUrlContext context;

	protected QueryPathResolver pathResolver = new DefaultQueryPathResolver();

	public DefaultQuerySpecUrlMapper() {
		supportedOperators.add(FilterOperator.LIKE);
		supportedOperators.add(FilterOperator.EQ);
		supportedOperators.add(FilterOperator.NEQ);
		supportedOperators.add(FilterOperator.GT);
		supportedOperators.add(FilterOperator.GE);
		supportedOperators.add(FilterOperator.LT);
		supportedOperators.add(FilterOperator.LE);
	}

	@Override
	public void init(QuerySpecUrlContext ctx) {
		this.context = ctx;
		pathResolver.init(context);
	}

	/**
	 * @return true if attribute paths must be separated with ".". Ealier
	 * Crnk versions did made use of brackets
	 * "[attribute1][attribute2]".
	 */
	public boolean getEnforceDotPathSeparator() {
		return enforceDotPathSeparator;
	}

	public void setEnforceDotPathSeparator(boolean enforceDotPathSeparator) {
		this.enforceDotPathSeparator = enforceDotPathSeparator;
	}

	/**
	 * @return whether to allow to pass unknown paths in sort, filter, include and field parameters. Disabled by default.
	 */
	public boolean getAllowUnknownAttributes() {
		return pathResolver.getAllowUnknownAttributes();
	}

	public void setAllowUnknownAttributes(boolean allowUnknownAttributes) {
		pathResolver.setAllowUnknownAttributes(allowUnknownAttributes);
	}

	/**
	 * @return whether to map json to java names in {@link QuerySpec} for sort, filter, include and field parameters. True by
	 * default.
	 */
	public boolean getMapJsonNames() {
		return pathResolver.getMapJsonNames();
	}

	public void setMapJsonNames(boolean mapJsonNames) {
		pathResolver.setMapJsonNames(mapJsonNames);
	}

	public FilterOperator getDefaultOperator() {
		return defaultOperator;
	}

	public void setDefaultOperator(FilterOperator defaultOperator) {
		this.defaultOperator = defaultOperator;
	}

	public Set<FilterOperator> getSupportedOperators() {
		return supportedOperators;
	}

	public void addSupportedOperator(FilterOperator supportedOperator) {
		this.supportedOperators.add(supportedOperator);
	}


	protected QuerySpec createQuerySpec(ResourceInformation resourceInformation) {
		return new QuerySpec(resourceInformation);
	}

	@Override
	public void init(QuerySpecDeserializerContext ctx) {
		init(new QuerySpecUrlContext() {
			@Override
			public ResourceRegistry getResourceRegistry() {
				return ctx.getResourceRegistry();
			}

			@Override
			public TypeParser getTypeParser() {
				return ctx.getTypeParser();
			}
		});
	}

	@Override
	public QuerySpec deserialize(ResourceInformation resourceInformation, Map<String, Set<String>> parameterMap) {
		QuerySpec rootQuerySpec = createQuerySpec(resourceInformation);

		List<QueryParameter> parameters = parseParameters(parameterMap, resourceInformation);
		Map<String, Set<String>> pageParameters = new HashMap<>();
		for (QueryParameter parameter : parameters) {
			QuerySpec querySpec = rootQuerySpec;
			if (parameter.getResourceInformation() != null) {
				querySpec = rootQuerySpec.getQuerySpec(parameter.getResourceInformation());
				if (querySpec == null) {
					querySpec = rootQuerySpec.getOrCreateQuerySpec(parameter.getResourceInformation());
				}
			}
			switch (parameter.getType()) {
				case SORT:
					deserializeSort(querySpec, parameter);
					break;
				case FILTER:
					deserializeFilter(querySpec, parameter);
					break;
				case INCLUDE:
					deserializeIncludes(querySpec, parameter);
					break;
				case FIELDS:
					deserializeFields(querySpec, parameter);
					break;
				case PAGE:
					pageParameters.put(parameter.getPagingType(), parameter.getValues());
					break;
				default:
					deserializeUnknown(querySpec, parameter);
			}
		}

		if (resourceInformation.getPagingBehavior() == null && !pageParameters.isEmpty()) {
			throw new IllegalStateException("Instance of PagingBehavior must be provided");
		}

		if (resourceInformation.getPagingBehavior() != null) {
			rootQuerySpec.setPagingSpec(resourceInformation.getPagingBehavior().deserialize(pageParameters));
		}

		return rootQuerySpec;
	}

	private static void put(Map<String, Set<String>> map, String key, String value) {
		map.put(key, new HashSet<>(Arrays.asList(value)));
	}

	private String toJsonPath(ResourceInformation resourceInformation, List<String> attributePath) {
		QueryPathSpec pathSpec =
				pathResolver.resolve(resourceInformation, attributePath, QueryPathResolver.NamingType.JAVA, null);
		return StringUtils.join(".", pathSpec.getAttributePath());
	}

	protected String addResourceType(QueryParameterType type, String key, ResourceInformation resourceInformation) {
		String resourceType = resourceInformation.getResourceType();
		return type.toString().toLowerCase() + "[" + resourceType + "]" + (key != null ? key : "");
	}

	protected static String serializeValue(Object value) {
		if (value == null) {
			return NULL_VALUE_STRING;
		}
		return value.toString();
	}

	@Override
	public Map<String, Set<String>> serialize(QuerySpec querySpec) {
		Map<String, Set<String>> map = new HashMap<>();
		serialize(querySpec, map, querySpec);
		return map;
	}

	protected void serialize(QuerySpec querySpec, Map<String, Set<String>> map, QuerySpec parentQuerySpec) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		String resourceType = querySpec.getResourceType();
		ResourceInformation resourceInformation;
		if (resourceType == null) {
			RegistryEntry entry = resourceRegistry.getEntry(querySpec.getResourceClass());
			if (entry == null) {
				throw new RepositoryNotFoundException(querySpec.getResourceClass());
			}
			resourceInformation = entry.getResourceInformation();
		}
		else {
			RegistryEntry entry = resourceRegistry.getEntry(querySpec.getResourceType());
			if (entry == null) {
				// model may not be available on client side in case of dynamic client with Resource.class
				resourceInformation = null;
			}
			else {
				resourceInformation = entry.getResourceInformation();
			}
		}

		serializeFilters(querySpec, resourceInformation, map);
		serializeSorting(querySpec, resourceInformation, map);
		serializeIncludedFields(querySpec, resourceInformation, map);
		serializeIncludedRelations(querySpec, resourceInformation, map);
		RegistryEntry entry = resourceRegistry.getEntry(parentQuerySpec.getResourceClass());
		if (entry != null && entry.getResourceInformation() != null
				&& entry.getResourceInformation().getPagingBehavior() != null) {
			PagingBehavior pagingBehavior = entry.getResourceInformation().getPagingBehavior();
			map.putAll(pagingBehavior.serialize(querySpec.getPagingSpec(), resourceType));
		}

		for (QuerySpec relatedSpec : querySpec.getRelatedSpecs().values()) {
			serialize(relatedSpec, map, querySpec);
		}
	}

	protected void serializeFilters(QuerySpec querySpec, ResourceInformation resourceInformation, Map<String, Set<String>> map) {
		for (FilterSpec filterSpec : querySpec.getFilters()) {
			if (filterSpec.hasExpressions()) {
				throw new UnsupportedOperationException("filter expressions like and and or not yet supported");
			}
			String attrKey =
					"[" + toJsonPath(resourceInformation, filterSpec.getAttributePath()) + "][" + filterSpec.getOperator()
							.getName() + "]";
			String key = addResourceType(QueryParameterType.FILTER, attrKey, resourceInformation);

			if (filterSpec.getValue() instanceof Collection) {
				Collection<?> col = filterSpec.getValue();
				Set<String> values = new HashSet<>();
				for (Object elem : col) {
					values.add(serializeValue(elem));
				}
				map.put(key, values);
			}
			else {
				String value = serializeValue(filterSpec.getValue());
				put(map, key, value);
			}
		}
	}

	public void serializeSorting(QuerySpec querySpec, ResourceInformation resourceInformation, Map<String, Set<String>> map) {
		if (!querySpec.getSort().isEmpty()) {
			String key = addResourceType(QueryParameterType.SORT, null, resourceInformation);

			StringBuilder builder = new StringBuilder();
			for (SortSpec filterSpec : querySpec.getSort()) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				if (filterSpec.getDirection() == Direction.DESC) {
					builder.append("-");
				}
				builder.append(toJsonPath(resourceInformation, filterSpec.getAttributePath()));
			}
			put(map, key, builder.toString());
		}
	}

	protected void serializeIncludedFields(QuerySpec querySpec, ResourceInformation resourceInformation,
			Map<String, Set<String>> map) {
		if (!querySpec.getIncludedFields().isEmpty()) {
			String key = addResourceType(QueryParameterType.FIELDS, null, resourceInformation);

			StringBuilder builder = new StringBuilder();
			for (IncludeFieldSpec includedField : querySpec.getIncludedFields()) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				builder.append(toJsonPath(resourceInformation, includedField.getAttributePath()));
			}
			put(map, key, builder.toString());
		}
	}

	protected void serializeIncludedRelations(QuerySpec querySpec, ResourceInformation resourceInformation,
			Map<String, Set<String>> map) {
		if (!querySpec.getIncludedRelations().isEmpty()) {
			String key = addResourceType(QueryParameterType.INCLUDE, null, resourceInformation);

			StringBuilder builder = new StringBuilder();
			for (IncludeRelationSpec includedField : querySpec.getIncludedRelations()) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				builder.append(toJsonPath(resourceInformation, includedField.getAttributePath()));
			}
			put(map, key, builder.toString());
		}
	}


	protected void deserializeIncludes(QuerySpec querySpec, QueryParameter parameter) {
		for (String values : parameter.getValues()) {
			for (String value : splitValues(values)) {
				List<String> attributePath = splitAttributePath(value, parameter);

				ResourceInformation resourceInformation = parameter.getResourceInformation();
				QueryPathSpec resolvedPath = pathResolver
						.resolve(resourceInformation, attributePath, QueryPathResolver.NamingType.JSON, parameter.getName());
				querySpec.includeRelation(resolvedPath.getAttributePath());
			}
		}
	}

	private String[] splitValues(String values) {
		return values.split(",");
	}

	protected void deserializeFields(QuerySpec querySpec, QueryParameter parameter) {
		ResourceInformation resourceInformation = parameter.getResourceInformation();
		for (String values : parameter.getValues()) {
			for (String value : splitValues(values)) {
				List<String> attributePath = splitAttributePath(value, parameter);
				QueryPathSpec resolvedPath = pathResolver
						.resolve(resourceInformation, attributePath, QueryPathResolver.NamingType.JSON, parameter.getName());
				querySpec.includeField(resolvedPath.getAttributePath());
			}
		}
	}

	protected void deserializeFilter(QuerySpec querySpec, QueryParameter parameter) {
		ResourceInformation resourceInformation = parameter.getResourceInformation();
		QueryPathSpec resolvedPath = pathResolver
				.resolve(resourceInformation, parameter.getAttributePath(), QueryPathResolver.NamingType.JSON,
						parameter.getName());
		Class attributeType = ClassUtils.getRawType(resolvedPath.getValueType());

		Set<Object> typedValues = new HashSet<>();
		for (String stringValue : parameter.getValues()) {
			try {
				if (NULL_VALUE_STRING.equals(stringValue)) {
					typedValues.add(null);
				}
				else if (attributeType != Object.class) {
					TypeParser typeParser = context.getTypeParser();
					Object value = typeParser.parse(stringValue, (Class) attributeType);
					typedValues.add(value);
				}
				else {
					typedValues.add(stringValue);
				}
			}
			catch (ParserException e) {
				if (ignoreParseExceptions) {
					typedValues.add(stringValue);
					LOGGER.debug("failed to parse {}", parameter);
				}
				else {
					throw new ParametersDeserializationException(parameter.toString(), e);
				}
			}
		}
		Object value = typedValues.size() == 1 ? typedValues.iterator().next() : typedValues;

		querySpec.addFilter(new FilterSpec(resolvedPath.getAttributePath(), parameter.getOperator(), value));
	}


	private void deserializeSort(QuerySpec querySpec, QueryParameter parameter) {
		ResourceInformation resourceInformation = parameter.getResourceInformation();
		for (String values : parameter.getValues()) {
			for (String value : splitValues(values)) {
				boolean desc = value.startsWith("-");
				if (desc) {
					value = value.substring(1);
				}
				List<String> attributePath = splitAttributePath(value, parameter);

				QueryPathSpec resolvedPath = pathResolver
						.resolve(resourceInformation, attributePath, QueryPathResolver.NamingType.JSON, parameter.getName());

				Direction dir = desc ? Direction.DESC : Direction.ASC;
				querySpec.addSort(new SortSpec(resolvedPath.getAttributePath(), dir));
			}
		}
	}

	protected void deserializeUnknown(QuerySpec querySpec, QueryParameter parameter) {
		if (!allowUnknownParameters) {
			throw new ParametersDeserializationException(parameter.getName());
		}
	}

	protected List<QueryParameter> parseParameters(Map<String, Set<String>> params,
			ResourceInformation rootResourceInformation) {
		List<QueryParameter> list = new ArrayList<>();
		Set<Map.Entry<String, Set<String>>> entrySet = params.entrySet();
		for (Map.Entry<String, Set<String>> entry : entrySet) {
			list.add(parseParameter(entry.getKey(), entry.getValue(), rootResourceInformation));
		}
		return list;
	}

	protected QueryParameter parseParameter(String parameterName, Set<String> values,
			ResourceInformation rootResourceInformation) {
		int typeSep = parameterName.indexOf('[');
		String strParamType = typeSep != -1 ? parameterName.substring(0, typeSep) : parameterName;

		QueryParameterType paramType;
		try {
			paramType = QueryParameterType.valueOf(strParamType.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			paramType = QueryParameterType.UNKNOWN;
		}

		List<String> elements = parseParameterNameArguments(parameterName, typeSep);

		QueryParameter param = new QueryParameter(context.getTypeParser());
		param.setName(parameterName);
		param.setType(paramType);
		param.setValues(values);

		if (paramType == QueryParameterType.FILTER && elements.size() >= 1) {
			parseFilterParameterName(param, elements, rootResourceInformation);
		}
		else if (paramType == QueryParameterType.PAGE && elements.size() == 1) {
			param.setResourceInformation(rootResourceInformation);
			param.setPagingType(elements.get(0));
		}
		else if (paramType == QueryParameterType.PAGE && elements.size() == 2) {
			param.setResourceInformation(getResourceInformation(elements.get(0), parameterName));
			param.setPagingType(elements.get(1));
		}
		else if (paramType == QueryParameterType.UNKNOWN) {
			param.setResourceInformation(null);
		}
		else if (elements.size() == 1) {
			param.setResourceInformation(getResourceInformation(elements.get(0), parameterName));
		}
		else {
			param.setResourceInformation(rootResourceInformation);
		}
		if (param.getOperator() == null) {
			param.setOperator(defaultOperator);
		}
		return param;
	}

	protected List<String> parseParameterNameArguments(String parameterName, int typeSep) {
		List<String> elements = new ArrayList<>();
		if (typeSep != -1) {
			String parameterNameSuffix = parameterName.substring(typeSep);
			if (!parameterNameSuffix.startsWith("[") || !parameterNameSuffix.endsWith("]")) {
				throw new ParametersDeserializationException("expected not [ resp. ] in legacy " +
						parameterName);
			}
			elements.addAll(Arrays.asList(parameterNameSuffix.substring(1, parameterNameSuffix.length() - 1).split("\\]\\[")));
		}
		return elements;
	}

	protected void parseFilterParameterName(QueryParameter param, List<String> elements,
			ResourceInformation rootResourceInformation) {
		// check whether last element is an operator
		parseFilterOperator(param, elements);

		if (elements.isEmpty()) {
			throw new ParametersDeserializationException("failed to parse " + param.getName() + ", expected "
					+ "([resourceType])[attr1.attr2]([operator])");
		}
		if (enforceDotPathSeparator && elements.size() > 2) {
			throw new ParametersDeserializationException(
					"failed to parse " + param.getName() + ", expected ([resourceType])[attr1.attr2]([operator])");
		}
		if (enforceDotPathSeparator && elements.size() == 2) {
			param.setResourceInformation(getResourceInformation(elements.get(0), param.getName()));
			param.setAttributePath(Arrays.asList(elements.get(1).split("\\.")));
		}
		else if (enforceDotPathSeparator && elements.size() == 1) {
			param.setResourceInformation(rootResourceInformation);
			param.setAttributePath(Arrays.asList(elements.get(0).split("\\.")));
		}
		else {
			legacyParseFilterParameterName(param, elements, rootResourceInformation);
		}
	}

	protected void legacyParseFilterParameterName(QueryParameter param, List<String> elements,
			ResourceInformation rootResourceInformation) {
		// check whether first element is a type or attribute, this
		// can cause problems if names clash, so use
		// enforceDotPathSeparator!
		if (isResourceType(elements.get(0))) {
			param.setResourceInformation(getResourceInformation(elements.get(0), param.getName()));
			elements.remove(0);
		}
		else {
			param.setResourceInformation(rootResourceInformation);
		}
		ArrayList<String> attributePath = new ArrayList<>();
		for (String element : elements) {
			attributePath.addAll(Arrays.asList(element.split("\\.")));
		}
		param.setAttributePath(attributePath);
	}

	protected void parseFilterOperator(QueryParameter param, List<String> elements) {
		String lastElement = elements.get(elements.size() - 1);
		FilterOperator operator = findOperator(lastElement);
		if (operator != null) {
			elements.remove(elements.size() - 1);
		}
		else {
			operator = defaultOperator;
		}
		param.setOperator(operator);
	}

	protected boolean isResourceType(String resourceType) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		return resourceRegistry.getEntry(resourceType) != null;
	}

	protected FilterOperator findOperator(String lastElement) {
		for (FilterOperator op : supportedOperators) {
			if (op.getName().equalsIgnoreCase(lastElement)) {
				return op;
			}
		}
		return null;
	}

	protected ResourceInformation getResourceInformation(String resourceType, String parameterName) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceType);
		if (registryEntry == null) {
			throw new ParametersDeserializationException("failed to parse parameter " + parameterName + ", resourceType=" +
					resourceType + " not found");
		}
		return registryEntry.getResourceInformation();
	}

	protected List<String> splitAttributePath(String pathString, QueryParameter param) {
		return Arrays.asList(pathString.split("\\."));
	}

	public boolean isIgnoreParseExceptions() {
		return ignoreParseExceptions;
	}

	public void setIgnoreParseExceptions(boolean ignoreParseExceptions) {
		this.ignoreParseExceptions = ignoreParseExceptions;
	}

	public boolean isAllowUnknownParameters() {
		return allowUnknownParameters;
	}

	public void setAllowUnknownParameters(final boolean allowUnknownParameters) {
		this.allowUnknownParameters = allowUnknownParameters;
	}

	public boolean getAllowUnknownParameters() {
		return allowUnknownParameters;
	}

	public QueryPathResolver getPathResolver() {
		return pathResolver;
	}
}
