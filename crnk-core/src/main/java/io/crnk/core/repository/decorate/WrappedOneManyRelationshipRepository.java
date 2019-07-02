package io.crnk.core.repository.decorate;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;
import java.util.Map;

/**
 * Decorator combination of {@link OneRelationshipRepository} and {@link ManyRelationshipRepository} repositories.
 *
 * Can be used to combine implementations of {@link WrappedOneRelationshipRepository} and {@link WrappedOneRelationshipRepository}
 * repositories when decorating a {@link RelationshipRepository}.
 *
 * @author Stas Melnichuk
 */
public class WrappedOneManyRelationshipRepository<T, I, D, J> extends WrappedRelationshipRepository<T, I, D, J> {

	private final OneRelationshipRepository<T, I, D, J> oneRelationshipRepository;
	private final ManyRelationshipRepository<T, I, D, J> manyRelationshipRepository;

	public WrappedOneManyRelationshipRepository(RelationshipRepository<T, I, D, J> wrappedRepository,
												OneRelationshipRepository<T, I, D, J> oneRelationshipRepository,
												ManyRelationshipRepository<T, I, D, J> manyRelationshipRepository) {
		super(wrappedRepository);
		this.oneRelationshipRepository = oneRelationshipRepository;
		this.manyRelationshipRepository = manyRelationshipRepository;
	}

	// region One Relationship Repository Methods

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		oneRelationshipRepository.setRelation(source, targetId, fieldName);
	}

	@Override
	public Map<I, D> findOneRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
		return oneRelationshipRepository.findOneRelations(sourceIds, fieldName, querySpec);
	}

	// endregion

	// region Many Relationship Repository Methods

	@Override
	public void setRelations(T source, Collection<J> targetIds, String fieldName) {
		manyRelationshipRepository.setRelations(source, targetIds, fieldName);
	}

	@Override
	public void addRelations(T source, Collection<J> targetIds, String fieldName) {
		manyRelationshipRepository.addRelations(source, targetIds, fieldName);
	}

	@Override
	public void removeRelations(T source, Collection<J> targetIds, String fieldName) {
		manyRelationshipRepository.removeRelations(source, targetIds, fieldName);
	}

	@Override
	public Map<I, ResourceList<D>> findManyRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
		return manyRelationshipRepository.findManyRelations(sourceIds, fieldName, querySpec);
	}

	// endregion

}
