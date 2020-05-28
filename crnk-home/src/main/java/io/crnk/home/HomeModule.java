package io.crnk.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.utils.Predicate;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtensionAware;
import io.crnk.core.queryspec.mapper.UrlBuilder;
import io.crnk.core.utils.Prioritizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a list of available resources in the root directory.
 */
public class HomeModule implements Module, ModuleExtensionAware<HomeModuleExtension> {

	protected static Logger LOGGER = LoggerFactory.getLogger(HomeModule.class);

	public static final String JSON_HOME_CONTENT_TYPE = "application/json-home";

	public static final String JSON_CONTENT_TYPE = "application/json";

	private List<HomeModuleExtension> extensions;

	private HomeFormat defaultFormat;

	private ModuleContext moduleContext;

	private HttpRequestProcessor requestProcessor;

	private boolean potentialFilterIssues = false;

	private List<Predicate<HttpRequestContext>> pathFilters = new ArrayList<>();

	// protected for CDI
	protected HomeModule() {
	}

	public static HomeModule create() {
		return create(HomeFormat.JSON_API);
	}

	public static HomeModule create(HomeFormat defaultFormat) {
		HomeModule module = new HomeModule();
		module.defaultFormat = defaultFormat;
		return module;
	}

	/**
	 * Allows to customize to which request paths the Home module should trigger. Useful when letting the home module work next to non-Crnk endpoints.
	 */
	public void addPathFilter(Predicate<HttpRequestContext> pathFilter) {
		pathFilters.add(pathFilter);
	}

	protected HttpRequestProcessor getRequestProcessor() {
		return requestProcessor;
	}

	@Override
	public String getModuleName() {
		return "home";
	}

	@Override
	public void setupModule(final ModuleContext context) {
		this.moduleContext = context;
		requestProcessor = new HomeHttpRequestProcessor();
		context.addHttpRequestProcessor(requestProcessor);
		context.addFilter(new HomeDocumentFilter());
	}

	class HomeDocumentFilter implements DocumentFilter {

		@Override
		public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
			UrlBuilder urlBuilder = moduleContext.getModuleRegistry().getUrlBuilder();

			Response response = chain.doFilter(filterRequestContext);
			QueryContext queryContext = filterRequestContext.getQueryAdapter().getQueryContext();
			JsonPath jsonPath = filterRequestContext.getJsonPath();
			if (jsonPath != null && jsonPath.isCollection() && queryContext != null) {
				// provide listings within repositories for further sub-repositories, e.g.
				// /api/tasks doing a listing for /api/tasks/history next to showing /api/tasks/{id}

				String baseUrl = moduleContext.getModuleRegistry().getResourceRegistry().getServiceUrlProvider().getUrl();

				String requestPath = queryContext.getRequestPath();
				ObjectMapper objectMapper = moduleContext.getObjectMapper();
				List<String> listings = list(requestPath, queryContext);
				if (!listings.isEmpty()) {
					Document document = response.getDocument();
					ObjectNode links = document.getLinks();
					if (links == null) {
						links = objectMapper.createObjectNode();
						document.setLinks(links);
					}
					for (String listing : listings) {
						String url = urlBuilder.filterUrl(UrlUtils.concat(baseUrl, requestPath, listing), queryContext);
						links.put(listing, url);
					}
				}
			}
			return response;
		}
	}

	class HomeHttpRequestProcessor implements HttpRequestProcessor, Prioritizable {

		@Override
		public boolean supportsAsync() {
			return true;
		}

		@Override
		public boolean accepts(HttpRequestContext requestContext) {
			String path = requestContext.getPath();
			if (!path.endsWith("/") || !requestContext.getMethod().equalsIgnoreCase(HttpMethod.GET.toString())) {
				LOGGER.debug("accepts return false due to accept header mismatch");
				return false;
			}

			boolean acceptsHome = requestContext.accepts(JSON_HOME_CONTENT_TYPE);
			boolean acceptsJsonApi = requestContext.accepts(HttpHeaders.JSONAPI_CONTENT_TYPE);
			boolean acceptsJson = requestContext.accepts(HttpHeaders.JSON_CONTENT_TYPE);
			boolean acceptsAny = requestContext.acceptsAny();
			if (!(acceptsHome || acceptsAny || acceptsJson || acceptsJsonApi)) {
				LOGGER.debug("accepts return false due to accept header mismatch");
				return false;
			}

			for (Predicate<HttpRequestContext> pathFilter : pathFilters) {
				if (!pathFilter.test(requestContext)) {
					LOGGER.debug("accepts return false due to path filter: {}", pathFilter);
					return false;
				}
			}

			QueryContext queryContext = requestContext.getQueryContext();
			ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
			PathBuilder pathBuilder = new PathBuilder(resourceRegistry, moduleContext.getTypeParser());
			JsonPath jsonPath = pathBuilder.build(path, queryContext);

			// check no repository with that path
			if (jsonPath == null) {
				// check there are children to display
				String requestPath = requestContext.getPath();
				List<String> pathList = list(requestPath, queryContext);
				boolean accepted = path.equals("/") || !pathList.isEmpty();
				LOGGER.debug(accepted ? "accepted to server: path={}" : "rejected since no listings available: path={}", requestPath);
				return accepted;
			}
			return false;

		}

		@Override
		public Result<HttpResponse> processAsync(HttpRequestContext requestContext) {
			LOGGER.debug("processing request");
			ResultFactory resultFactory = moduleContext.getResultFactory();
			boolean jsonapi = requestContext.accepts(HttpHeaders.JSONAPI_CONTENT_TYPE);
			try {
				QueryContext queryContext = requestContext.getQueryContext();
				String requestPath = requestContext.getPath();
				List<String> pathList = list(requestPath, queryContext);

				boolean useJsonHome = defaultFormat == HomeFormat.JSON_HOME || requestContext.accepts(JSON_HOME_CONTENT_TYPE);
				HttpResponse response;
				if (useJsonHome) {
					LOGGER.debug("using JSON home format");
					boolean acceptsHome = requestContext.accepts(JSON_HOME_CONTENT_TYPE);
					response = writeJsonHome(queryContext, pathList);
					if (acceptsHome) {
						response.setContentType(JSON_HOME_CONTENT_TYPE);
					}
					else {
						response.setContentType(JSON_CONTENT_TYPE);
					}
				}
				else {
					LOGGER.debug("using JSON API format");
					response = getResponse(queryContext, pathList);
					if (jsonapi) {
						response.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
					}
					else {
						response.setContentType(JSON_CONTENT_TYPE);
					}
				}
				return resultFactory.just(response);
			}
			catch (Exception e) {
				ExceptionMapperRegistry exceptionMapperRegistry = moduleContext.getExceptionMapperRegistry();
				Response response = exceptionMapperRegistry.toErrorResponse(e);
				String contentType = jsonapi ? HttpHeaders.JSONAPI_CONTENT_TYPE : JSON_CONTENT_TYPE;
				return resultFactory.just(response.toHttpResponse(moduleContext.getObjectMapper(), contentType));
			}
		}

		@Override
		public int getPriority() {
			return 1000; // low prio to not override others like ui module
		}
	}

	public List<String> list(String requestPath, QueryContext queryContext) {
		Set<String> pathSet = new HashSet<>();
		ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
		boolean hasEntries = false;
		boolean hasUnfilteredEntries = false;
		for (RegistryEntry resourceEntry : resourceRegistry.getEntries()) {
			ResourceRepositoryInformation repositoryInformation = resourceEntry.getRepositoryInformation();
			if (repositoryInformation == null || !repositoryInformation.isExposed()) {
				continue;
			}
			hasEntries = true;
			if (moduleContext.getResourceFilterDirectory()
					.get(resourceEntry.getResourceInformation(), HttpMethod.GET, queryContext) == FilterBehavior.NONE) {
				ResourceInformation resourceInformation = resourceEntry.getResourceInformation();
				String resourceType = resourceInformation.getResourcePath();
				pathSet.add("/" + resourceType);
				hasUnfilteredEntries = true;
			}
		}

		if (hasEntries && !hasUnfilteredEntries) {
			potentialFilterIssues = true;
			LOGGER.warn(
					"all resources have been filtered for current request/user. Make sure SecurityModule and related modules "
							+ "are "
							+ "properly configured."
			);
		}

		if (extensions != null) {
			for (HomeModuleExtension extension : extensions) {
				pathSet.addAll(extension.getPaths());
			}
		}

		Set<String> filteredPathSet = pathSet.stream().map(it -> getChildName(requestPath, it))
				.filter(it -> it != null)
				.collect(Collectors.toSet());

		// favor repositories over children, e.g. list /tasks rather than /tasks/ for /tasks/history
		List<String> pathList = new ArrayList<>();
		filteredPathSet.stream()
				.filter(it -> !it.endsWith("/") || !filteredPathSet.contains(UrlUtils.removeTrailingSlash(it)))
				.forEach(pathList::add);

		// sort for readability
		Collections.sort(pathList);
		return pathList;
	}

	public boolean hasPotentialFilterIssues() {
		return potentialFilterIssues;
	}

	private String getChildName(String requestPath, String it) {
		// e.g. /tasks must provide listing for /tasks/history but non of /tasksXy
		requestPath = UrlUtils.removeTrailingSlash(requestPath) + "/";

		if (it.startsWith(requestPath)) {
			String subPath = it.substring(requestPath.length());
			int sep = subPath.indexOf('/');
			return sep == -1 ? subPath : subPath.substring(0, sep + 1);
		}
		return null;
	}

	private HttpResponse getResponse(QueryContext queryContext, List<String> pathList) {
		ObjectMapper objectMapper = moduleContext.getObjectMapper();
		UrlBuilder urlBuilder = moduleContext.getModuleRegistry().getUrlBuilder();

		String listingPath = getListingPath(queryContext.getRequestContext());

		ObjectNode node = objectMapper.createObjectNode();
		ObjectNode links = node.putObject("links");
		for (String path : pathList) {
			String id = UrlUtils.removeTrailingSlash(path);
			String url = UrlUtils.concat(listingPath, path);
			url = urlBuilder.filterUrl(url, queryContext);
			links.put(id, url);
		}

		Map<String, String> serverInfo = moduleContext.getModuleRegistry().getServerInfo();
		if (serverInfo != null && !serverInfo.isEmpty()) {
			node.set("jsonapi", objectMapper.valueToTree(serverInfo));
		}

		String json;
		try {
			json = objectMapper.writeValueAsString(node);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}

		HttpResponse response = new HttpResponse();
		response.setStatusCode(200);
		response.setBody(json);
		return response;
	}

	private String getListingPath(HttpRequestContext requestContext) {
		return UrlUtils.concat(moduleContext.getResourceRegistry().getServiceUrlProvider().getUrl(), requestContext.getPath());
	}

	private HttpResponse writeJsonHome(QueryContext queryContext, List<String> pathList) {
		ObjectMapper objectMapper = moduleContext.getObjectMapper();

		UrlBuilder urlBuilder = moduleContext.getModuleRegistry().getUrlBuilder();

		ObjectNode node = objectMapper.createObjectNode();
		ObjectNode resourcesNode = node.putObject("resources");
		for (String path : pathList) {
			String tag = "tag:" + UrlUtils.removeTrailingSlash(path);
			String href = urlBuilder.filterUrl(path, queryContext);
			ObjectNode resourceNode = resourcesNode.putObject(tag);
			resourceNode.put("href", href);
		}
		String json;
		try {
			json = objectMapper.writeValueAsString(node);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}

		HttpResponse response = new HttpResponse();
		response.setStatusCode(200);
		response.setBody(json);
		return response;
	}


	@Override
	public void setExtensions(List<HomeModuleExtension> extensions) {
		this.extensions = extensions;
	}

	@Override
	public void init() {

	}
}
