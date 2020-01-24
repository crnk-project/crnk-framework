package io.crnk.data.jpa.internal;

import java.util.List;
import javax.persistence.EntityManager;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.data.jpa.JpaRepositoryConfig;

/**
 * Base class for resource repositories backed by a JPA entity. Subclasses may perform
 * arbitrary customizations. A JpaModule must be registered to Crnk in order for this
 * repository to work.
 *
 * @param <T>
 */
public abstract class JpaRepositoryBase<T> implements JpaRepositoryConfigSupplier {

    protected JpaRepositoryConfig<T> repositoryConfig;

    protected JpaRepositoryBase(JpaRepositoryConfig<T> repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

    public EntityManager getEntityManager() {
        EntityManager em = repositoryConfig.getQueryFactory().getEntityManager();
        PreconditionUtil.verify(em != null,
                "entityManager not available, make sure to pass an EntityManager to XyQueryFactory.create(em) when using " + getClass().getSimpleName()
                        + " in standalone without a JpaModule");
        return em;
    }

    @Override
    public JpaRepositoryConfig<T> getRepositoryConfig() {
        return repositoryConfig;
    }

    protected <D> D getUnique(List<D> list, Object id) {
        if (list.isEmpty()) {
            throw new ResourceNotFoundException(
                    "resource not found: type=" + repositoryConfig.getResourceClass().getSimpleName() + " id=" + id);
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new IllegalStateException(
                    "unique result expected: " + repositoryConfig.getResourceClass().getSimpleName() + " id=" + id);
        }
    }


}
