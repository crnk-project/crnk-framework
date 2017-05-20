package io.crnk.legacy.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.legacy.repository.annotations.*;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * An adapter for annotation-based resource repository. Stores references to document methods and call o proper one
 * when a document method has to be called. This class is instantiated in {@link io.crnk.legacy.registry.RepositoryInstanceBuilder}
 */
public class AnnotatedResourceRepositoryAdapter<T, ID extends Serializable>
		extends AnnotatedRepositoryAdapter<T> {

	private Method findOneMethod;
	private Method findAllMethod;
	private Method findAllWithIds;
	private Method saveMethod;
	private Method deleteMethod;

	public AnnotatedResourceRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
		super(implementationObject, parametersFactory);
	}

	public Object findOne(ID id, QueryAdapter queryAdapter) {
		Class<JsonApiFindOne> annotationType = JsonApiFindOne.class;
		if (findOneMethod == null) {
			findOneMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
		}
		return invokeOperation(findOneMethod, annotationType, new Object[]{id}, queryAdapter);
	}

	public Object findAll(QueryAdapter queryAdapter) {
		Class<JsonApiFindAll> annotationType = JsonApiFindAll.class;
		if (findAllMethod == null) {
			findAllMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
		}
		return invokeOperation(findAllMethod, annotationType, new Object[]{}, queryAdapter);
	}

	public Object findAll(Iterable<ID> ids, QueryAdapter queryAdapter) {
		Class<JsonApiFindAllWithIds> annotationType = JsonApiFindAllWithIds.class;
		if (findAllWithIds == null) {
			findAllWithIds = ClassUtils.findMethodWith(implementationClass, annotationType);
		}
		return invokeOperation(findAllWithIds, annotationType, new Object[]{ids}, queryAdapter);
	}

	public <S extends T> Object save(S entity) {
		Class<JsonApiSave> annotationType = JsonApiSave.class;
		if (saveMethod == null) {
			saveMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
		}
		return invokeOperation(saveMethod, annotationType, new Object[]{entity});
	}

	public void delete(ID id, QueryAdapter queryAdapter) {
		Class<JsonApiDelete> annotationType = JsonApiDelete.class;
		if (deleteMethod == null) {
			deleteMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
		}
		invokeOperation(deleteMethod, annotationType, new Object[]{id}, queryAdapter);
	}
}
