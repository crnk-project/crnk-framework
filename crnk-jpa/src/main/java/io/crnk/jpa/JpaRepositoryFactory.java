package io.crnk.jpa;

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
	<T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
																				 JpaRepositoryConfig<T> config);

	/**
	 * Creates a relationship document that maps an entity relationship to a JSON API endpoint. The provided document classes do not necessarily have to be
	 * an entity class. The JpaModule is checked whether there is a mapping available.
	 *
	 * @param <S>                 source document type
	 * @param <I>                 source identifier type
	 * @param <T>                 target document type
	 * @param <J>                 target identifier type
	 * @param module              managing the document
	 * @param sourceResourceClass representing the source of the relation (entity or mapped dto)
	 * @param config              for this document
	 * @return created document
	 */
	<S, I extends Serializable, T, J extends Serializable> JpaRelationshipRepository<S, I, T, J> createRelationshipRepository(
			JpaModule module, Class<S> sourceResourceClass, JpaRepositoryConfig<T> config);

}
