package io.crnk.home;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;

import java.io.IOException;

/**
 * Displays a list of available resources in the root directory.
 */
public class HomeModule implements Module {

	public static final String JSON_HOME_CONTENT_TYPE = "application/json-home";

	public static final String JSON_CONTENT_TYPE = "application/json";


	private HomeModule() {

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

					ResourceRegistry resourceRegistry = context.getResourceRegistry();
					for (RegistryEntry resourceEntry : resourceRegistry.getResources()) {
						RepositoryInformation repositoryInformation = resourceEntry.getRepositoryInformation();
						if (resourceEntry.getRepositoryInformation() != null) {
							String resourceType = repositoryInformation.getResourceInformation().getResourceType();
							String tag = "tag:" + resourceType;
							String href = "/" + resourceType + "/";
							ObjectNode resourceNode = resourcesNode.putObject(tag);
							resourceNode.put("href", href);
						}
					}

					String json = objectMapper.writeValueAsString(node);
					boolean acceptsHome = requestContext.accepts(JSON_HOME_CONTENT_TYPE);
					if (acceptsHome) {
						requestContext.setContentType(JSON_HOME_CONTENT_TYPE);
					} else {
						requestContext.setContentType(JSON_CONTENT_TYPE);
					}
					requestContext.setResponse(200, json);
				}
			}
		});
	}


}
