package io.crnk.data.jpa;

/**
 * Exposes a JPA entity as ResourceRepository.
 */
public class JpaEntityRepository<T, I > extends JpaEntityRepositoryBase<T, I> {

    public JpaEntityRepository(JpaRepositoryConfig<T> config) {
        super(config);
    }
}
