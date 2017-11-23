package io.crnk.jpa;

import java.io.Serializable;
import java.util.List;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryExecutor;
import io.crnk.jpa.query.Tuple;

/**
 * Can be registered with the JpaModule and gets notified about all kinds of document events.
 * The filter then has to possiblity to do all kinds of changes.
 */
public interface JpaRepositoryFilter {

	/**
	 * Decorate or customize the given JPA resource repository upon creation.
	 *
	 * @param repository to decorate
	 * @return decorated or customized repository
	 */
	<T, I extends Serializable> JpaEntityRepository<T, I> filterCreation(JpaEntityRepository<T, I> repository);

	/**
	 * Decorate or customize the given JPA resource repository upon creation.
	 *
	 * @param relationship to decorate
	 * @return decorated or customized repository
	 */
	<S, I extends Serializable, T, J extends Serializable> JpaRelationshipRepository<S, I, T, J> filterCreation(
			JpaRelationshipRepository<S, I, T, J> repository);

	/**
	 * Specifies whether any of the filter methods should be executed for the given resourceType.;
	 *
	 * @param resourceType to filter
	 * @return true if filter should be used for the given resouceType.
	 */
	boolean accept(Class<?> resourceType);

	/**
	 * Allows to customize the querySpec before creating the query.
	 *
	 * @param repository where the query is executed
	 * @param querySpec to filter
	 * @return filtered querySpec
	 */
	QuerySpec filterQuerySpec(Object repository, QuerySpec querySpec);

	/**
	 * Allows to customize the query.
	 *
	 * @param <T> document class
	 * @param repository where the query is executed
	 * @param querySpec that is used to query
	 * @param query to filter
	 * @return filtered query
	 */
	<T> JpaQuery<T> filterQuery(Object repository, QuerySpec querySpec, JpaQuery<T> query);

	/**
	 * Allows to customize the query executor.
	 *
	 * @param <T> document class
	 * @param repository where the query is executed
	 * @param querySpec that is used to query
	 * @param executor to filter
	 * @return filtered executor
	 */
	<T> JpaQueryExecutor<T> filterExecutor(Object repository, QuerySpec querySpec, JpaQueryExecutor<T> executor);

	/**
	 * Allows to filter tuples and return the filtered slistet.
	 *
	 * @param repository where the query is executed
	 * @param querySpec that is used to query
	 * @param tuples to filter
	 * @return filtered list of tuples
	 */
	List<Tuple> filterTuples(Object repository, QuerySpec querySpec, List<Tuple> tuples);

	/**
	 * Allows to filter resources and return the filtered list.
	 *
	 * @param <T> document class
	 * @param repository where the query is executed
	 * @param querySpec that is used to query
	 * @param resources to filter
	 * @return filtered list of resources
	 */
	<T> ResourceList<T> filterResults(Object repository, QuerySpec querySpec, ResourceList<T> resources);

}
