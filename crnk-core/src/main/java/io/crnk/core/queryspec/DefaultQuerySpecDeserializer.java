package io.crnk.core.queryspec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PropertyException;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.parser.ParserException;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.resource.RestrictedQueryParamsMembers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps url parameters to QuerySpec.
 */
public class DefaultQuerySpecDeserializer implements QuerySpecDeserializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultQuerySpecDeserializer.class);

	private FilterOperator defaultOperator = FilterOperator.EQ;

	private Set<FilterOperator> supportedOperators = new HashSet<>();

	private boolean allowUnknownAttributes = false;

	private boolean enforceDotPathSeparator = false;

	private boolean ignoreParseExceptions;

	private boolean allowUnknownParameters = false;

	private QuerySpecDeserializerContext context;

	public DefaultQuerySpecDeserializer() {
		supportedOperators.add(FilterOperator.LIKE);
		supportedOperators.add(FilterOperator.EQ);
		supportedOperators.add(FilterOperator.NEQ);
		supportedOperators.add(FilterOperator.GT);
		supportedOperators.add(FilterOperator.GE);
		supportedOperators.add(FilterOperator.LT);
		supportedOperators.add(FilterOperator.LE);
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

	public boolean getAllowUnknownAttributes() {
		return allowUnknownAttributes;
	}

	public void setAllowUnknownAttributes(boolean allowUnknownAttributes) {
		this.allowUnknownAttributes = allowUnknownAttributes;
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

	@Override
	public void init(QuerySpecDeserializerContext ctx) {
		this.context = ctx;
	}

	protected QuerySpec createQuerySpec(ResourceInformation resourceInformation) {
		return new QuerySpec(resourceInformation);
	}

	@Override
	public QuerySpec deserialize(ResourceInformation resourceInformation, Map<String, Set<String>> parameterMap) {
		QuerySpec rootQuerySpec = createQuerySpec(resourceInformation);

		List<Parameter> parameters = parseParameters(parameterMap, resourceInformation);
		Map<String, Set<String>> pageParameters = new HashMap<>();
		for (Parameter parameter : parameters) {
			QuerySpec querySpec = rootQuerySpec;
			if (parameter.resourceInformation != null) {
				querySpec = rootQuerySpec.getQuerySpec(parameter.resourceInformation);
				if (querySpec == null) {
					querySpec = rootQuerySpec.getOrCreateQuerySpec(parameter.resourceInformation);
				}
			}
			switch (parameter.paramType) {
				case sort:
					deserializeSort(querySpec, parameter);
					break;
				case filter:
					deserializeFilter(querySpec, parameter);
					break;
				case include:
					deserializeIncludes(querySpec, parameter);
					break;
				case fields:
					deserializeFields(querySpec, parameter);
					break;
				case page:
					pageParameters.put(parameter.pageParameter, parameter.getValues());
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

	private void deserializeIncludes(QuerySpec querySpec, Parameter parameter) {
		for (String values : parameter.values) {
			for (String value : splitValues(values)) {
				List<String> attributePath = splitAttributePath(value, parameter);
				querySpec.includeRelation(attributePath);
			}
		}
	}

	private String[] splitValues(String values) {
		return values.split(",");
	}

	protected void deserializeFields(QuerySpec querySpec, Parameter parameter) {
		for (String values : parameter.values) {
			for (String value : splitValues(values)) {
				List<String> attributePath = splitAttributePath(value, parameter);
				querySpec.includeField(attributePath);
			}
		}
	}

	protected void deserializeFilter(QuerySpec querySpec, Parameter parameter) {
		Class<?> attributeType = getAttributeType(querySpec, parameter.attributePath);
		Set<Object> typedValues = new HashSet<>();
		for (String stringValue : parameter.values) {
			try {
				TypeParser typeParser = context.getTypeParser();
				Object value = typeParser.parse(stringValue, (Class) attributeType);
				typedValues.add(value);
			} catch (ParserException e) {
				if (ignoreParseExceptions) {
					typedValues.add(stringValue);
					LOGGER.debug("failed to parse {}", parameter);
				} else {
					throw new ParametersDeserializationException(parameter.toString(), e);
				}
			}
		}
		Object value = typedValues.size() == 1 ? typedValues.iterator().next() : typedValues;

		querySpec.addFilter(new FilterSpec(parameter.attributePath, parameter.operator, value));
	}

	protected Class<?> getAttributeType(QuerySpec querySpec, List<String> attributePath) {
		try {
			if (attributePath == null) {
				// no attribute specified, query string expected, use String
				return String.class;
			}
			Class<?> current = querySpec.getResourceClass();
			for (String propertyName : attributePath) {
				current = getAttributeType(current, propertyName);
			}
			return current;
		} catch (PropertyException e) {
			if (allowUnknownAttributes) {
				return String.class;
			} else {
				throw e;
			}
		}
	}

	protected Class<?> getAttributeType(Class<?> clazz, String propertyName) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		if (resourceRegistry.hasEntry(clazz)) {
			RegistryEntry entry = resourceRegistry.getEntryForClass(clazz);
			ResourceInformation resourceInformation = entry.getResourceInformation();
			ResourceField field = resourceInformation.findFieldByName(propertyName);
			if (field != null) {
				return field.getType();
			}
		}
		return PropertyUtils.getPropertyClass(clazz, propertyName);
	}

	private void deserializeSort(QuerySpec querySpec, Parameter parameter) {
		for (String values : parameter.values) {
			for (String value : splitValues(values)) {
				boolean desc = value.startsWith("-");
				if (desc) {
					value = value.substring(1);
				}
				List<String> attributePath = splitAttributePath(value, parameter);
				Direction dir = desc ? Direction.DESC : Direction.ASC;
				querySpec.addSort(new SortSpec(attributePath, dir));
			}
		}
	}

	protected void deserializeUnknown(QuerySpec querySpec, Parameter parameter) {
		if (!allowUnknownParameters) {
			throw new ParametersDeserializationException(parameter.name);
		}
	}

	private List<Parameter> parseParameters(Map<String, Set<String>> params, ResourceInformation rootResourceInformation) {
		List<Parameter> list = new ArrayList<>();
		Set<Entry<String, Set<String>>> entrySet = params.entrySet();
		for (Entry<String, Set<String>> entry : entrySet) {
			list.add(parseParameter(entry.getKey(), entry.getValue(), rootResourceInformation));
		}
		return list;
	}

	private Parameter parseParameter(String parameterName, Set<String> values, ResourceInformation rootResourceInformation) {
		int typeSep = parameterName.indexOf('[');
		String strParamType = typeSep != -1 ? parameterName.substring(0, typeSep) : parameterName;

		RestrictedQueryParamsMembers paramType;
		try {
			paramType = RestrictedQueryParamsMembers.valueOf(strParamType.toLowerCase());
		} catch (IllegalArgumentException e) {
			paramType = RestrictedQueryParamsMembers.unknown;
		}

		List<String> elements = parseParameterNameArguments(parameterName, typeSep);

		Parameter param = new Parameter();
		param.name = parameterName;
		param.paramType = paramType;
		param.strParamType = strParamType;
		param.values = values;

		if (paramType == RestrictedQueryParamsMembers.filter && elements.size() >= 1) {
			parseFilterParameterName(param, elements, rootResourceInformation);
		} else if (paramType == RestrictedQueryParamsMembers.page && elements.size() == 1) {
			param.resourceInformation = rootResourceInformation;
			param.pageParameter = elements.get(0);
		} else if (paramType == RestrictedQueryParamsMembers.page && elements.size() == 2) {
			param.resourceInformation = getResourceInformation(elements.get(0), parameterName);
			param.pageParameter = elements.get(1);
		} else if (paramType == RestrictedQueryParamsMembers.unknown) {
			param.resourceInformation = null;
		} else if (elements.size() == 1) {
			param.resourceInformation = getResourceInformation(elements.get(0), parameterName);
		} else {
			param.resourceInformation = rootResourceInformation;
		}
		if (param.operator == null) {
			param.operator = defaultOperator;
		}
		return param;
	}

	private List<String> parseParameterNameArguments(String parameterName, int typeSep) {
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

	private void parseFilterParameterName(Parameter param, List<String> elements, ResourceInformation rootResourceInformation) {
		// check whether last element is an operator
		parseFilterOperator(param, elements);

		if (elements.isEmpty()) {
			throw new ParametersDeserializationException("failed to parse " + param.name + ", expected "
					+ "([resourceType])[attr1.attr2]([operator])");
		}
		if (enforceDotPathSeparator && elements.size() > 2) {
			throw new ParametersDeserializationException(
					"failed to parse " + param.name + ", expected ([resourceType])[attr1.attr2]([operator])");
		}
		if (enforceDotPathSeparator && elements.size() == 2) {
			param.resourceInformation = getResourceInformation(elements.get(0), param.name);
			param.attributePath = Arrays.asList(elements.get(1).split("\\."));
		} else if (enforceDotPathSeparator && elements.size() == 1) {
			param.resourceInformation = rootResourceInformation;
			param.attributePath = Arrays.asList(elements.get(0).split("\\."));
		} else {
			legacyParseFilterParameterName(param, elements, rootResourceInformation);
		}
	}

	private void legacyParseFilterParameterName(Parameter param, List<String> elements,
												ResourceInformation rootResourceInformation) {
		// check whether first element is a type or attribute, this
		// can cause problems if names clash, so use
		// enforceDotPathSeparator!
		if (isResourceType(elements.get(0))) {
			param.resourceInformation = getResourceInformation(elements.get(0), param.name);
			elements.remove(0);
		} else {
			param.resourceInformation = rootResourceInformation;
		}
		param.attributePath = new ArrayList<>();
		for (String element : elements) {
			param.attributePath.addAll(Arrays.asList(element.split("\\.")));
		}
	}

	private void parseFilterOperator(Parameter param, List<String> elements) {
		String lastElement = elements.get(elements.size() - 1);
		param.operator = findOperator(lastElement);
		if (param.operator != null) {
			elements.remove(elements.size() - 1);
		} else {
			param.operator = defaultOperator;
		}
	}

	private boolean isResourceType(String resourceType) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		return resourceRegistry.getEntry(resourceType) != null;
	}

	private FilterOperator findOperator(String lastElement) {
		for (FilterOperator op : supportedOperators) {
			if (op.getName().equalsIgnoreCase(lastElement)) {
				return op;
			}
		}
		return null;
	}

	private ResourceInformation getResourceInformation(String resourceType, String parameterName) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceType);
		if (registryEntry == null) {
			throw new ParametersDeserializationException("failed to parse parameter " + parameterName + ", resourceType=" +
					resourceType + " not found");
		}
		return registryEntry.getResourceInformation();
	}

	private List<String> splitAttributePath(String pathString, Parameter param) {
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

	public boolean getAllowUknownParameters() {
		return allowUnknownParameters;
	}

	public class Parameter {

		private String pageParameter;

		private String name;

		private RestrictedQueryParamsMembers paramType;

		private String strParamType;

		private ResourceInformation resourceInformation;

		private FilterOperator operator;

		private List<String> attributePath;

		private Set<String> values;

		public Long getLongValue() {
			if (values.size() != 1) {
				throw new ParametersDeserializationException("expected a Long for " + toString());
			}
			try {
				return Long.parseLong(values.iterator().next());
			} catch (NumberFormatException e) {
				throw new ParametersDeserializationException("expected a Long for " + toString());
			}
		}

		@Override
		public String toString() {
			return name + "=" + values;
		}

		public String getPageParameter() {
			return pageParameter;
		}

		public String getName() {
			return name;
		}

		public RestrictedQueryParamsMembers getParamType() {
			return paramType;
		}

		public String getStrParamType() {
			return strParamType;
		}

		public ResourceInformation getResourceInformation() {
			return resourceInformation;
		}

		public FilterOperator getOperator() {
			return operator;
		}

		public List<String> getAttributePath() {
			return attributePath;
		}

		public Set<String> getValues() {
			return values;
		}
	}
}