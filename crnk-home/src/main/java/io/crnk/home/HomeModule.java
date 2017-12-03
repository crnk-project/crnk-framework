package io.crnk.home;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtensionAware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Displays a list of available resources in the root directory.
 */
public class HomeModule implements Module, ModuleExtensionAware<HomeModuleExtension> {

	public static final String JSON_HOME_CONTENT_TYPE = "application/json-home";

	public static final String JSON_CONTENT_TYPE = "application/json";

	private List<HomeModuleExtension> extensions;

	// protected for CDI
	protected HomeModule() {
	}

	public static HomeModule create() {
		return new HomeModule();
	}

	public static boolean isHomeRequest(HttpRequestContext requestContext) {
		boolean isRoot = UrlUtils.removeTrailingSlash(requestContext.getPath()).isEmpty();
		if (!isRoot) {
			return false;
		}

		boolean acceptsHome = requestContext.accepts(JSON_HOME_CONTENT_TYPE);
		boolean acceptsAny = requestContext.acceptsAny();
		if (!(acceptsHome || acceptsAny)) {
			return false;
		}
		return requestContext.getMethod().equalsIgnoreCase(HttpMethod.GET.toString());
	}

	@Override
	public String getModuleName() {
		return "home";
	}

	@Override
	public void setupModule(final ModuleContext context) {
		context.addHttpRequestProcessor(new HttpRequestProcessor() {
			@Override
			public void process(HttpRequestContext requestContext) throws IOException {
				if (isHomeRequest(requestContext)) {
					ObjectMapper objectMapper = context.getObjectMapper();
					ObjectNode node = objectMapper.createObjectNode();
					ObjectNode resourcesNode = node.putObject("resources");

					Set<String> pathSet = new HashSet<>();

					ResourceRegistry resourceRegistry = context.getResourceRegistry();
					for (RegistryEntry resourceEntry : resourceRegistry.getResources()) {
						RepositoryInformation repositoryInformation = resourceEntry.getRepositoryInformation();
						if (repositoryInformation != null &&
								context.getResourceFilterDirectory().get(resourceEntry.getResourceInformation(), HttpMethod.GET)
										== FilterBehavior.NONE) {
							ResourceInformation resourceInformation = resourceEntry.getResourceInformation();
							String resourceType = resourceInformation.getResourceType();
							pathSet.add("/" + resourceType);
						}
					}

					if (extensions != null) {
						for (HomeModuleExtension extension : extensions) {
							pathSet.addAll(extension.getPaths());
						}
					}

					List<String> pathList = new ArrayList<>(pathSet);
					Collections.sort(pathList);
					for (String path : pathList) {
						String tag = "tag:" + path;
						String href = path;
						ObjectNode resourceNode = resourcesNode.putObject(tag);
						resourceNode.put("href", href);
					}

					String json = objectMapper.writeValueAsString(node);
					boolean acceptsHome = requestContext.accepts(JSON_HOME_CONTENT_TYPE);
					if (acceptsHome) {
						requestContext.setContentType(JSON_HOME_CONTENT_TYPE);
					}
					else {
						requestContext.setContentType(JSON_CONTENT_TYPE);
					}
					requestContext.setResponse(200, json);
				}
			}
		});
	}


	@Override
	public void setExtensions(List<HomeModuleExtension> extensions) {
		this.extensions = extensions;
	}

	@Override
	public void init() {

	}
}
