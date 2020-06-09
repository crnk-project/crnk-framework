package io.crnk.core.repository.foward;

import java.util.Collection;
import java.util.List;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.BulkRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.foward.strategy.ForwardingGetStrategy;
import io.crnk.core.repository.foward.strategy.ForwardingSetStrategy;
import io.crnk.core.repository.foward.strategy.ForwardingStrategyContext;
import io.crnk.core.repository.foward.strategy.GetFromOppositeStrategy;
import io.crnk.core.repository.foward.strategy.GetFromOwnerStrategy;
import io.crnk.core.repository.foward.strategy.SetOppositeStrategy;
import io.crnk.core.repository.foward.strategy.SetOwnerStrategy;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a RelationshipRepository for relationships making use of one or both adjacent resource repositories. This class assumes that one of those resource repositories is
 * able to directly load relationships or there identifiers (see {@link JsonApiRelationId}). In many cases it might be the later. If so, the identifiers are then normally resolved
 * with the matching resource repository.
 * <p>
 * {@link ForwardingGetStrategy} and {@link ForwardingSetStrategy} provide the implementation how those resource repositories are accessed. There are implementations for the owning
 * and opposite side of a relationship. Get and set are separated since various permutation of there use are thinkable.
 * <p>
 * This class provides the basis to implement {@link io.crnk.core.resource.annotations.RelationshipRepositoryBehavior}.
 */
public class ForwardingRelationshipRepository<T, I, D, J>
		implements BulkRelationshipRepository<T, I, D, J>, ResourceRegistryAware, HttpRequestContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForwardingRelationshipRepository.class);

	private RelationshipMatcher matcher;

	private ForwardingGetStrategy getStrategy;

	private ForwardingSetStrategy setStrategy;

	private Class<T> sourceClass;

	private String sourceType;

	private ForwardingStrategyContext context;

	private HttpRequestContextProvider requestContextProvider;

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
		LOGGER.debug("set relation {}={} with {}", fieldName, targetId, setStrategy);
		QueryContext queryContext = getQueryContext();
		setStrategy.setRelation(source, targetId, fieldName, queryContext);
	}

	@Override
	public void setRelations(T source, Collection<J> targetIds, String fieldName) {
		LOGGER.debug("set relations {}={} with {}", fieldName, targetIds, setStrategy);
		QueryContext queryContext = getQueryContext();
		setStrategy.setRelations(source, targetIds, fieldName, queryContext);
	}

	@Override
	public void addRelations(T source, Collection<J> targetIds, String fieldName) {
		LOGGER.debug("add relations {}={} with {}", fieldName, targetIds, setStrategy);
		QueryContext queryContext = getQueryContext();
		setStrategy.addRelations(source, targetIds, fieldName, queryContext);
	}

	@Override
	public void removeRelations(T source, Collection<J> targetIds, String fieldName) {
		LOGGER.debug("remove relations {}={} with {}", fieldName, targetIds, setStrategy);
		QueryContext queryContext = getQueryContext();
		setStrategy.removeRelations(source, targetIds, fieldName, queryContext);
	}

	@Override
	public MultivaluedMap<I, D> findTargets(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
		LOGGER.debug("findTargets {} for {} with {}", fieldName, querySpec, getStrategy);
		QueryContext queryContext = getQueryContext();
		MultivaluedMap<I, D> targets = getStrategy.findTargets(sourceIds, fieldName, querySpec, queryContext);

		for (List<D> list : targets.values()) {
			// top-level links must be cleared because they will differ for relationships
			// TODO clear others as well
			if (list instanceof ResourceList) {
				ResourceList<D> resourceList = (ResourceList<D>) list;
				LinksInformation links = resourceList.getLinks();
				if (links instanceof SelfLinksInformation) {
					((SelfLinksInformation) links).setSelf((Link) null);
				}
			}
		}
		return targets;
	}

	protected QueryContext getQueryContext() {
		HttpRequestContext requestContext = requestContextProvider.getRequestContext();
		return requestContext != null ? requestContext.getQueryContext() : new QueryContext();
	}

	@Override
	public void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider) {
		this.requestContextProvider = requestContextProvider;
	}
}
