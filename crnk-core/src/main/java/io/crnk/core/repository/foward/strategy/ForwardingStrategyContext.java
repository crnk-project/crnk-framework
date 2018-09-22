package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class ForwardingStrategyContext {

	private final ResourceRegistry resourceRegistry;

	private final String sourceType;

	private final Class sourceClass;

	public ForwardingStrategyContext(ResourceRegistry resourceRegistry, String sourceType, Class sourceClass) {
		this.resourceRegistry = Objects.requireNonNull(resourceRegistry);
		this.sourceType = sourceType;
		this.sourceClass = sourceClass;
	}

	public RegistryEntry getSourceEntry() {
		return sourceType != null ? resourceRegistry.getEntry(sourceType) :
				resourceRegistry.getEntry(sourceClass);
	}

	public RegistryEntry getTargetEntry(ResourceField field) {
		return resourceRegistry.getEntry(field.getOppositeResourceType());
	}

	public QueryAdapter createQueryAdapter(QuerySpec querySpec, QueryContext queryContext) {
		return new QuerySpecAdapter(querySpec, resourceRegistry, queryContext);
	}

	protected QueryAdapter createSaveQueryAdapter(String fieldName, QueryContext queryContext) {
		QuerySpec querySpec = createSourceQuerySpec();
		querySpec.includeRelation(Arrays.asList(fieldName));
		return new QuerySpecAdapter(querySpec, resourceRegistry, queryContext);
	}

	private QuerySpec createSourceQuerySpec() {
		RegistryEntry sourceEntry = getSourceEntry();
		ResourceInformation resourceInformation = sourceEntry.getResourceInformation();
		return new QuerySpec(resourceInformation.getResourceClass(), resourceInformation.getResourceType());
	}

	public <Q> Iterable<Q> findAll(RegistryEntry entry, Iterable<?> targetIds, QueryContext queryContext) {
		ResourceRepositoryAdapter targetAdapter = entry.getResourceRepository();
		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(entry.getResourceInformation()), resourceRegistry, queryContext);
		return (Iterable) targetAdapter.findAll(targetIds, queryAdapter).get().getEntity();
	}

	public <Q> Q findOne(RegistryEntry entry, Serializable id, QueryContext queryContext) {
		ResourceRepositoryAdapter targetAdapter = entry.getResourceRepository();
		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(entry.getResourceInformation()), resourceRegistry, queryContext);
		return (Q) targetAdapter.findOne(id, queryAdapter).get().getEntity();
	}
}
