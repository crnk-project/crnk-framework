package io.crnk.core.engine.information.repository;

/**
 * A builder which creates RepositoryInformation instances from repositories or their classes.
 * Building information from the actual object class is a bit more flexible as it allows
 * the reuse of the same class for multiple, different repositories classes.
 */
public interface RepositoryInformationProvider {

	/**
	 * @param repositoryClass class
	 * @return true if this builder can process the provided repository class
	 */
	boolean accept(Class<?> repositoryClass);

	/**
	 * @param repository resource
	 * @return true if this builder can process the provided repository class
	 */
	boolean accept(Object repository);

	/**
	 * @param repository object
	 * @return RepositoryInformation for the provided repository class.
	 */
	RepositoryInformation build(Object repository, RepositoryInformationProviderContext context);

	/**
	 * @param repositoryClass repository class
	 * @return RepositoryInformation for the provided repository class.
	 */
	RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationProviderContext context);

}
