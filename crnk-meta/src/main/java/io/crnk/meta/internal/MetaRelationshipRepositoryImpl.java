package io.crnk.meta.internal;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Supplier;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MetaRelationshipRepositoryImpl implements RelationshipRepositoryV2<MetaElement, String, MetaElement, String> {

	private Supplier<MetaLookup> lookupSupplier;

	private Class<? extends MetaElement> sourceResourceClass;

	private Class<? extends MetaElement> targetResourceClass;

	public MetaRelationshipRepositoryImpl(Supplier<MetaLookup> lookupSupplier, Class<? extends MetaElement> sourceClass,
										  Class<? extends MetaElement> targetClass) {
		this.lookupSupplier = lookupSupplier;
		this.sourceResourceClass = sourceClass;
		this.targetResourceClass = targetClass;
	}

	@Override
	public MetaElement findOneTarget(String sourceId, String fieldName, QuerySpec querySpec) {
		MetaElement source = getSource(sourceId);
		Object value = adjustForRequest(PropertyUtils.getProperty(source, fieldName));
		return (MetaElement) value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResourceList<MetaElement> findManyTargets(String sourceId, String fieldName, QuerySpec querySpec) {
		MetaElement source = getSource(sourceId);
		Object value = adjustForRequest(PropertyUtils.getProperty(source, fieldName));
		return querySpec.apply((Collection<MetaElement>) value);
	}

	private MetaElement getSource(String sourceId) {
		MetaLookup lookup = lookupSupplier.get();
		MetaElement source = lookup.getMetaById().get(sourceId);
		if (source == null) {
			throw new ResourceNotFoundException(sourceId);
		}
		return source;
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

	private Object adjustForRequest(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof MetaElement) {
			return MetaUtils.adjustForRequest(lookupSupplier.get(), (MetaElement) object);
		} else {
			PreconditionUtil.assertTrue("expected collection", object instanceof Collection);
			List<MetaElement> results = new ArrayList<>();
			MetaLookup lookup = lookupSupplier.get();
			for (MetaElement element : ((Collection<MetaElement>) object)) {
				MetaElement adjustedElement = MetaUtils.adjustForRequest(lookup, element);
				if (adjustedElement != null) {
					results.add(adjustedElement);
				}
			}
			return results;
		}
	}
}