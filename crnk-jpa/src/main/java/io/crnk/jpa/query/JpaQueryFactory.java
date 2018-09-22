package io.crnk.jpa.query;

import javax.persistence.EntityManager;
import java.util.List;

public interface JpaQueryFactory {

	/**
	 * Called by the JPA module ones initalized.
	 *
	 * @param context to access entityManager, meta-data, etc.
	 */
	void initalize(JpaQueryFactoryContext context);

	/**
	 * Builds a new query for the given entity class.
	 *
	 * @param <T>         entity
	 * @param entityClass to query
	 * @return query
	 */
	<T> JpaQuery<T> query(Class<T> entityClass);

	/**
	 * Builds a new query for the given attribute. Used to retrieve relations of
	 * an entity.
	 *
	 * @param <T>               parent entity
	 * @param parentEntityClass to fetch children from
	 * @param childrenAttrName  on parent entity
	 * @param parentIds         to retrieve the children from
	 * @return query
	 */
	<T> JpaQuery<T> query(Class<?> parentEntityClass, String childrenAttrName, String parentKey, List<?> parentIds);

	/**
	 * @return ComputedAttributeRegistry holding registered computed attributes
	 */
	ComputedAttributeRegistry getComputedAttributes();

	EntityManager getEntityManager();

}
