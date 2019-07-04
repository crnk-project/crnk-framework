package io.crnk.core.repository.decorate;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.*;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;
import java.util.Map;

/**
 * Decorator combination of {@link OneRelationshipRepository} and {@link OneRelationshipRepository} wrapped repositories.
 *
 * Can be used to combine implementations of {@link WrappedOneRelationshipRepository} and {@link WrappedOneRelationshipRepository}
 * repositories when decorating an implementation of both {@link OneRelationshipRepository} and {@link OneRelationshipRepository} (like {@link RelationshipRepository}).
 *
 * @param <T> source class type
 * @param <I> T class id type
 * @param <D> target class type
 * @param <J> D class id type
 * @param <K> decorated original repository type
 *
 * @author Stas Melnichuk
 */
public class WrappedOneManyRelationshipRepository<T, I, D, J, K extends OneRelationshipRepository & ManyRelationshipRepository>
		implements OneRelationshipRepository<T, I, D, J>, ManyRelationshipRepository<T, I, D, J>, Wrapper {

	protected final K originalRepository;

	protected final OneRelationshipRepository<T, I, D, J> wrappedOneRelationshipRepository;
	protected final ManyRelationshipRepository<T, I, D, J> wrappedManyRelationshipRepository;

	/**
	 */
	public WrappedOneManyRelationshipRepository(
			K originalRepository,
			OneRelationshipRepository<T, I, D, J> wrappedOneRelationshipRepository,
			ManyRelationshipRepository<T, I, D, J> wrappedManyRelationshipRepository) {
		this.originalRepository = originalRepository;
		this.wrappedOneRelationshipRepository = wrappedOneRelationshipRepository;
		this.wrappedManyRelationshipRepository = wrappedManyRelationshipRepository;
	}

	@Override
	public RelationshipMatcher getMatcher() {
		return originalRepository.getMatcher();
	}

	// region One Relationship Repository Methods

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		wrappedOneRelationshipRepository.setRelation(source, targetId, fieldName);
	}

	@Override
	public Map<I, D> findOneRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
		return wrappedOneRelationshipRepository.findOneRelations(sourceIds, fieldName, querySpec);
	}

	// endregion

	// region Many Relationship Repository Methods

	@Override
	public void setRelations(T source, Collection<J> targetIds, String fieldName) {
		wrappedManyRelationshipRepository.setRelations(source, targetIds, fieldName);
	}

	@Override
	public void addRelations(T source, Collection<J> targetIds, String fieldName) {
		wrappedManyRelationshipRepository.addRelations(source, targetIds, fieldName);
	}

	@Override
	public void removeRelations(T source, Collection<J> targetIds, String fieldName) {
		wrappedManyRelationshipRepository.removeRelations(source, targetIds, fieldName);
	}

	@Override
	public Map<I, ResourceList<D>> findManyRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
		return wrappedManyRelationshipRepository.findManyRelations(sourceIds, fieldName, querySpec);
	}

	// endregion


	@Override
	public Object getWrappedObject() {
		return originalRepository;
	}
}
