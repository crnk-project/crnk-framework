package io.crnk.jpa;

import io.crnk.core.engine.information.resource.ResourceField;

import java.io.Serializable;

/**
 * Used to create document and relationship repositories for the provided
 * classes. By default {@link DefaultJpaRepositoryFactory}} is used.
 */
public interface JpaRepositoryFactory {

	/**
	 * Creates a document document that maps an entity to a JSON API endpoint. The provided document class not necessarily has to be
	 * an entity class. The JpaModule is checked whether there is a mapping available.
	 *
	 * @param <T>    document type
	 * @param <I>    identifier type
	 * @param module managing the document
	 * @param config for this document
	 * @return created document
	 */
	<T, I > JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
																				 JpaRepositoryConfig<T> config);

	/**
	 * Creates a relationship document that maps an entity relationship to a JSON API endpoint. The provided document classes do not necessarily have to be
	 * an entity class. The JpaModule is checked whether there is a mapping available.
	 *
	 * @param <S>           source document type
	 * @param <I>           source identifier type
	 * @param <T>           target document type
	 * @param <J>           target identifier type
	 * @param module        managing the document
	 * @param resourceField representing the source field of the relation (entity or mapped dto)
	 * @param config        for this document
	 * @return created document
	 */
	<S, I , T, J > JpaRelationshipRepository<S, I, T, J> createRelationshipRepository(
			JpaModule module, ResourceField resourceField, JpaRepositoryConfig<T> config);

}
