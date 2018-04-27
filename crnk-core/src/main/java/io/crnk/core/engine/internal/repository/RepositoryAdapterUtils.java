package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpecUrlBuilder;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;

public class RepositoryAdapterUtils {

	public static LinksInformation enrichLinksInformation(ModuleRegistry moduleRegistry, LinksInformation linksInformation,
			Object resources,
			RepositoryRequestSpec requestSpec) {
		QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
		LinksInformation enrichedLinksInformation = linksInformation;
		if (queryAdapter instanceof QuerySpecAdapter && resources instanceof ResourceList) {
			ResourceInformation responseResourceInformation = requestSpec.getResponseResourceInformation();
			PagingBehavior pagingBehavior = responseResourceInformation.getPagingBehavior();
			if (pagingBehavior != null && pagingBehavior.isRequired(queryAdapter.getPagingSpec())) {
				enrichedLinksInformation =
						enrichPageLinksInformation(moduleRegistry, enrichedLinksInformation, (ResourceList<?>) resources,
								queryAdapter, requestSpec);
			}
		}
		return enrichedLinksInformation;
	}

	private static LinksInformation enrichPageLinksInformation(ModuleRegistry moduleRegistry, LinksInformation linksInformation,
			ResourceList<?> resources,
			QueryAdapter queryAdapter, RepositoryRequestSpec requestSpec) {
		if (linksInformation == null) {
			// use default implementation if no link information
			// provided by resource
			linksInformation = new DefaultPagedLinksInformation();
		}
		if (linksInformation instanceof PagedLinksInformation) {
			PagingSpecUrlBuilder urlBuilder = new PagingSpecUrlBuilder(moduleRegistry, requestSpec);
			requestSpec.getResponseResourceInformation().getPagingBehavior()
					.build((PagedLinksInformation) linksInformation, resources, queryAdapter, urlBuilder);
		}
		return linksInformation;
	}

}
