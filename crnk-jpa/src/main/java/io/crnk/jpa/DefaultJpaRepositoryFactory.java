package io.crnk.jpa;

public class DefaultJpaRepositoryFactory implements JpaRepositoryFactory {

    @Override
    public <T, I> JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
                                                                   JpaRepositoryConfig<T> config) {

        return new JpaEntityRepository<>(config);
    }
}
