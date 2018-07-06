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
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtensionAware;
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
				return false;
			}

			boolean acceptsHome = requestContext.accepts(JSON_HOME_CONTENT_TYPE);
			boolean acceptsJsonApi = requestContext.accepts(HttpHeaders.JSONAPI_CONTENT_TYPE);
			boolean acceptsJson = requestContext.accepts(HttpHeaders.JSON_CONTENT_TYPE);
			boolean acceptsAny = requestContext.acceptsAny();
			if (!(acceptsHome || acceptsAny || acceptsJson || acceptsJsonApi)) {
				return false;
			}

			ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
			JsonPath jsonPath = new PathBuilder(resourceRegistry).build(path);

			// check no repository with that path
			if (jsonPath == null) {
				// check there are children to display
				QueryContext queryContext = requestContext.getQueryContext();
				String requestPath = requestContext.getPath();
				List<String> pathList = list(requestPath, queryContext);
				return path.equals("/") || !pathList.isEmpty();
			}
			return false;

		}

		@Override
		public Result<HttpResponse> processAsync(HttpRequestContext requestContext) {
			LOGGER.debug("processing request");

			QueryContext queryContext = requestContext.getQueryContext();
			String requestPath = requestContext.getPath();
			List<String> pathList = list(requestPath, queryContext);

			HttpResponse response;
			if (defaultFormat == HomeFormat.JSON_HOME || requestContext.accepts(JSON_HOME_CONTENT_TYPE)) {
				LOGGER.debug("using JSON home format");
				boolean acceptsHome = requestContext.accepts(JSON_HOME_CONTENT_TYPE);
				response = writeJsonHome(requestContext, pathList);
				if (acceptsHome) {
					response.setContentType(JSON_HOME_CONTENT_TYPE);
				}
				else {
					response.setContentType(JSON_CONTENT_TYPE);
				}
			}
			else {
				boolean jsonapi = requestContext.accepts(HttpHeaders.JSONAPI_CONTENT_TYPE);
				LOGGER.debug("using JSON API format");
				response = getResponse(requestContext, pathList);
				if (jsonapi) {
					response.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
				}
				else {
					response.setContentType(JSON_CONTENT_TYPE);
				}
			}

			ResultFactory resultFactory = moduleContext.getResultFactory();
			return resultFactory.just(response);
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
		for (RegistryEntry resourceEntry : resourceRegistry.getResources()) {
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

		pathSet = pathSet.stream().map(it -> getChildName(requestPath, it))
				.filter(it -> it != null).collect(Collectors.toSet());

		List<String> pathList = new ArrayList<>(pathSet);
		Collections.sort(pathList);
		return pathList;
	}

	public boolean hasPotentialFilterIssues() {
		return potentialFilterIssues;
	}

	private String getChildName(String requestPath, String it) {
		if (it.startsWith(requestPath)) {
			String subPath = it.substring(requestPath.length());
			int sep = subPath.indexOf('/');
			return sep == -1 ? subPath : subPath.substring(0, sep + 1);
		}
		return null;
	}

	private HttpResponse getResponse(HttpRequestContext requestContext, List<String> pathList) {
		ObjectMapper objectMapper = moduleContext.getObjectMapper();

		String baseUrl = UrlUtils.removeTrailingSlash(moduleContext.getResourceRegistry().getServiceUrlProvider().getUrl())
				+ requestContext.getPath();

		ObjectNode node = objectMapper.createObjectNode();
		ObjectNode links = node.putObject("links");
		for (String path : pathList) {
			String href = baseUrl + path;
			String id = UrlUtils.removeTrailingSlash(path);
			links.put(id, href);
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

	private HttpResponse writeJsonHome(HttpRequestContext requestContext, List<String> pathList) {
		ObjectMapper objectMapper = moduleContext.getObjectMapper();

		ObjectNode node = objectMapper.createObjectNode();
		ObjectNode resourcesNode = node.putObject("resources");
		for (String path : pathList) {
			String tag = "tag:" + UrlUtils.removeTrailingSlash(path);
			String href = path;
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
