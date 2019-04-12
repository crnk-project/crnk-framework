package io.crnk.jpa;

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
    <T, I> JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
                                                            JpaRepositoryConfig<T> config);

}
