package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpecUrlBuilder;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.list.ResourceList;

public class RepositoryAdapterUtils {

	public static LinksInformation enrichLinksInformation(ModuleRegistry moduleRegistry,
			LinksInformation linksInformation, Object resource,
			RepositoryRequestSpec requestSpec) {
		if (requestSpec.getQueryAdapter() instanceof QuerySpecAdapter && resource instanceof ResourceList) {
			ResourceList<?> resources = (ResourceList<?>) resource;
			linksInformation = enrichPageLinksInformation(moduleRegistry, linksInformation, resources, requestSpec);
			linksInformation = enrichSelfLinksInformation(linksInformation, requestSpec);
		}
		return linksInformation;
	}

	private static LinksInformation enrichSelfLinksInformation(LinksInformation linksInformation,
			RepositoryRequestSpec requestSpec) {
		ResourceInformation resourceInformation = requestSpec.getQueryAdapter().getResourceInformation();
		ResourceRegistry resourceRegistry = requestSpec.getQueryAdapter().getResourceRegistry();
		if (resourceRegistry != null && resourceInformation != null && linksInformation instanceof SelfLinksInformation) {
			((SelfLinksInformation) linksInformation).setSelf(resourceRegistry.getResourceUrl(resourceInformation));
		}
		return linksInformation;
	}

	private static LinksInformation enrichPageLinksInformation(ModuleRegistry moduleRegistry,
			LinksInformation linksInformation,
			ResourceList<?> resources,
			RepositoryRequestSpec requestSpec) {
		ResourceInformation responseResourceInformation = requestSpec.getResponseResourceInformation();

		RegistryEntry entry = moduleRegistry.getResourceRegistry().getEntry(responseResourceInformation.getResourceType());
		PreconditionUtil.verify(entry != null, "resourceType=%s not found", responseResourceInformation.getResourceType());
		PagingBehavior pagingBehavior = entry.getPagingBehavior();
		if (pagingBehavior != null && pagingBehavior.isRequired(requestSpec.getQueryAdapter().getPagingSpec())) {
			if (linksInformation == null) {
				// use default implementation if no link information provided by resource
				linksInformation = new DefaultPagedLinksInformation();
			}
			if (linksInformation instanceof PagedLinksInformation) {
				PagingSpecUrlBuilder urlBuilder = new PagingSpecUrlBuilder(moduleRegistry, requestSpec);
				pagingBehavior
						.build((PagedLinksInformation) linksInformation, resources, requestSpec.getQueryAdapter(), urlBuilder);
			}
		}
		return linksInformation;
	}
}
