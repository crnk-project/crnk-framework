package io.crnk.meta.internal;

import java.util.Collection;

import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;

public class MetaRelationshipRepository implements RelationshipRepositoryV2<MetaElement, String, MetaElement, String> {

	private MetaLookup lookup;

	private Class<? extends MetaElement> sourceResourceClass;

	private Class<? extends MetaElement> targetResourceClass;

	public MetaRelationshipRepository(MetaLookup lookup, Class<? extends MetaElement> sourceClass,
			Class<? extends MetaElement> targetClass) {
		this.lookup = lookup;
		this.sourceResourceClass = sourceClass;
		this.targetResourceClass = targetClass;
	}

	@Override
	public MetaElement findOneTarget(String sourceId, String fieldName, QuerySpec querySpec) {
		MetaElement source = lookup.getMetaById().get(sourceId);
		if (source == null) {
			throw new ResourceNotFoundException(sourceId);
		}
		Object value = PropertyUtils.getProperty(source, fieldName);
		if (!(value instanceof MetaElement) && value != null) {
			throw new IllegalStateException("relation " + fieldName + " is not of type MetaElement, got " + value);
		}
		return (MetaElement) value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResourceList<MetaElement> findManyTargets(String sourceId, String fieldName, QuerySpec querySpec) {
		MetaElement source = lookup.getMetaById().get(sourceId);
		if (source == null) {
			throw new ResourceNotFoundException(sourceId);
		}
		Object value = PropertyUtils.getProperty(source, fieldName);
		if (!(value instanceof Collection)) {
			throw new IllegalStateException("relation " + fieldName + " is not a collection, got " + value);
		}
		return querySpec.apply((Collection<MetaElement>) value);
	}

	@Override
	public Class<MetaElement> getSourceResourceClass() {
		return (Class<MetaElement>) sourceResourceClass;
	}

	@Override
	public Class<MetaElement> getTargetResourceClass() {
		return (Class<MetaElement>) targetResourceClass;
	}

	@Override
	public void setRelation(MetaElement source, String targetId, String fieldName) {
		throw newReadOnlyException();
	}

	@Override
	public void setRelations(MetaElement source, Iterable<String> targetIds, String fieldName) {
		throw newReadOnlyException();
	}

	@Override
	public void addRelations(MetaElement source, Iterable<String> targetIds, String fieldName) {
		throw newReadOnlyException();
	}

	@Override
	public void removeRelations(MetaElement source, Iterable<String> targetIds, String fieldName) {
		throw newReadOnlyException();
	}

	private UnsupportedOperationException newReadOnlyException() {
		return new UnsupportedOperationException("read-only");
	}
}