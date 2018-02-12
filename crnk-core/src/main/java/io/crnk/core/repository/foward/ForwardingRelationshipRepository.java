package io.crnk.core.repository.foward;

import java.io.Serializable;
import java.util.Arrays;

import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.BulkRelationshipRepositoryV2;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.foward.strategy.ForwardingGetStrategy;
import io.crnk.core.repository.foward.strategy.ForwardingSetStrategy;
import io.crnk.core.repository.foward.strategy.ForwardingStrategyContext;
import io.crnk.core.repository.foward.strategy.GetFromOppositeStrategy;
import io.crnk.core.repository.foward.strategy.GetFromOwnerStrategy;
import io.crnk.core.repository.foward.strategy.SetOppositeStrategy;
import io.crnk.core.repository.foward.strategy.SetOwnerStrategy;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

/**
 * Implements a RelationshipRepository for relationships making use of one or both adjacent resource repositories.
 * This class assumes that one of those resource repositories is able to directly load relationships or there identifiers
 * (see {@link JsonApiRelationId}). In many cases it might be the later. If so, the identifiers are then normally resolved
 * with the matching resource repository.
 * <p>
 * {@link ForwardingGetStrategy} and {@link ForwardingSetStrategy} provide the implementation how those resource repositories are
 * accessed. There are implementations for the owning and opposite side of a relationship. Get and set are separated
 * since various permutation of there use are thinkable.
 * <p>
 * This class provides the basis to implement {@link io.crnk.core.resource.annotations.RelationshipRepositoryBehavior}.
 */
public class ForwardingRelationshipRepository<T, I extends Serializable, D, J extends Serializable>
		implements BulkRelationshipRepositoryV2<T, I, D, J>, ResourceRegistryAware {


	private RelationshipMatcher matcher;

	private ForwardingGetStrategy getStrategy;

	private ForwardingSetStrategy setStrategy;

	private Class<T> sourceClass;

	private String sourceType;

	private ForwardingStrategyContext context;

	/**
	 * default constructor for CDI an other DI libraries
	 */
	protected ForwardingRelationshipRepository() {
	}

	public ForwardingRelationshipRepository(Class<T> sourceClass, RelationshipMatcher matcher, ForwardingDirection getDirection,
			ForwardingDirection setDirection) {
		this(sourceClass, matcher, toGetStrategy(getDirection), toSetStrategy(setDirection));
	}


	public ForwardingRelationshipRepository(Class<T> sourceClass, RelationshipMatcher matcher, ForwardingGetStrategy<T, I, D, J>
			getStrategy,
			ForwardingSetStrategy<T, I, D, J> setStrategy) {
		this.sourceClass = sourceClass;
		this.matcher = matcher;
		this.getStrategy = getStrategy;
		this.setStrategy = setStrategy;
	}

	public ForwardingRelationshipRepository(String sourceType, RelationshipMatcher matcher, ForwardingDirection getDirection,
			ForwardingDirection setDirection) {
		this(sourceType, matcher, toGetStrategy(getDirection), toSetStrategy(setDirection));
	}

	private static ForwardingGetStrategy toGetStrategy(ForwardingDirection direction) {
		return direction == ForwardingDirection.OWNER ? new GetFromOwnerStrategy() : new GetFromOppositeStrategy();
	}

	private static ForwardingSetStrategy toSetStrategy(ForwardingDirection direction) {
		return direction == ForwardingDirection.OWNER ? new SetOwnerStrategy() : new SetOppositeStrategy();
	}

	public ForwardingRelationshipRepository(String sourceType, RelationshipMatcher matcher, ForwardingGetStrategy<T, I, D, J>
			getStrategy, ForwardingSetStrategy<T, I, D, J> setStrategy) {
		this.sourceType = sourceType;
		this.matcher = matcher;
		this.getStrategy = getStrategy;
		this.setStrategy = setStrategy;
	}


	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		if (context == null) {
			// TODO prevent duplicate calls once legacy code is eliminated
			context = new ForwardingStrategyContext(resourceRegistry, sourceType, sourceClass);
			getStrategy.init(context);
			setStrategy.init(context);
		}
	}

	@Override
	public RelationshipMatcher getMatcher() {
		return matcher;
	}

	@Override
	public Class<T> getSourceResourceClass() {
		throw new UnsupportedOperationException("deprecated and no longer supported");
	}

	@Override
	public Class<D> getTargetResourceClass() {
		throw new UnsupportedOperationException("deprecated and no longer supported");
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		setStrategy.setRelation(source, targetId, fieldName);
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		setStrategy.setRelations(source, targetIds, fieldName);
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		setStrategy.addRelations(source, targetIds, fieldName);
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		setStrategy.removeRelations(source, targetIds, fieldName);
	}

	@Override
	public final D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, D> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return null;
		}
		return map.getUnique(sourceId);
	}

	@Override
	public final ResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, D> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return new DefaultResourceList<>();
		}
		return (ResourceList<D>) map.getList(sourceId);
	}


	@Override
	public MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) {
		return getStrategy.findTargets(sourceIds, fieldName, querySpec);
	}

}
