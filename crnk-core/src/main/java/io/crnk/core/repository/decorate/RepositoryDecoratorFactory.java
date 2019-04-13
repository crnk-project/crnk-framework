package io.crnk.core.repository.decorate;

/**
 * Allows to intercept calls to repositories by modules and make changes.
 */
public interface RepositoryDecoratorFactory {

    /**
     * Generic method allowing to decorate any kind of repository.
     *
     * @param repository
     * @return decorated object or original repository if no decoration should happen.
     */
    Object decorateRepository(Object repository);

}
