package io.crnk.data.jpa.internal.query;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.criteria.JoinType;

import io.crnk.data.jpa.internal.query.backend.JpaQueryBackend;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaMapAttribute;

public class JoinRegistry<F, E> {

	private Map<MetaAttributePath, F> joinMap = new HashMap<>();

	private JpaQueryBackend<F, ?, ?, E> backend;

	private AbstractJpaQueryImpl<?, ?> query;

	public JoinRegistry(JpaQueryBackend<F, ?, ?, E> backend, AbstractJpaQueryImpl<?, ?> query) {
		this.backend = backend;
		this.query = query;
	}

	protected static MetaAttributePath extractAssociationPath(MetaAttributePath path) {
		for (int i = path.length() - 1; i >= 0; i--) {
			MetaAttribute element = path.getElement(i);
			if (element.isAssociation()) {
				return path.subPath(0, i + 1);
			}
		}
		return new MetaAttributePath();
	}

	public E getEntityAttribute(MetaAttributePath attrPath) {
		return getEntityAttribute(attrPath, query.getDefaultJoinType());
	}

	public E getEntityAttribute(MetaAttributePath attrPath, JoinType defaultJoinType) {
		MetaAttributePath associationPath = extractAssociationPath(attrPath);
		MetaAttributePath primitivePath = attrPath.subPath(associationPath.length());

		@SuppressWarnings("unchecked")
		E from = (E) getOrCreateJoin(associationPath, defaultJoinType);
		if (primitivePath.length() == 0) {
			return from;
		}

		MetaAttributePath currentPath = associationPath;
		E criteriaPath = null;
		for (MetaAttribute pathElement : primitivePath) {
			currentPath = currentPath.concat(pathElement);

			E currentCriteriaPath = criteriaPath != null ? criteriaPath : from;
			if (pathElement instanceof MetaMapAttribute) {
				if (criteriaPath != null) {
					throw new IllegalStateException("Cannot join to map");
				}
				criteriaPath = joinMap(currentCriteriaPath, pathElement);
			}
			else {
				// we may need to downcast if attribute is defined on a subtype
				MetaDataObject parent = pathElement.getParent().asDataObject();
				Class<?> pathType = parent.getImplementationClass();
				Class<?> currentType = backend.getJavaElementType(currentCriteriaPath);
				boolean isSubType = !pathType.isAssignableFrom(currentType);
				if (isSubType) {
					currentCriteriaPath = backend.joinSubType(currentCriteriaPath, pathType);
				}
				criteriaPath = backend.getAttribute(currentCriteriaPath, pathElement);
			}
		}
		return criteriaPath;

	}

	private E joinMap(E currentCriteriaPath, MetaAttribute pathElement) {
		MetaMapAttribute mapPathElement = (MetaMapAttribute) pathElement;
		return backend.joinMapValue(currentCriteriaPath, pathElement, mapPathElement.getKey());
	}


	public F getOrCreateJoin(MetaAttributePath path, JoinType defaultJoinType) {
		if (path.length() == 0) {
			return backend.getRoot();
		}

		MetaAttributePath subPath = new MetaAttributePath();
		F from = backend.getRoot();

		for (int i = 0; i < path.length(); i++) {
			MetaAttribute pathElement = path.getElement(i);
			from = getOrCreateJoin(subPath, pathElement, defaultJoinType);
			subPath = subPath.concat(pathElement);
		}
		return from;
	}

	private F getOrCreateJoin(MetaAttributePath srcPath, MetaAttribute targetAttr, JoinType defaultJoinType) {
		MetaAttributePath path = srcPath.concat(targetAttr);
		F parent = joinMap.get(srcPath);
		F join = joinMap.get(path);
		if (join == null) {
			JoinType joinType = query.getJoinType(path, defaultJoinType);
			if(targetAttr instanceof MetaMapAttribute){
				MetaMapAttribute targetMapAttr = (MetaMapAttribute) targetAttr;
				join = backend.joinMapRelation(parent, targetMapAttr, targetMapAttr.getKey());
			}else {
				join = backend.doJoin(targetAttr, joinType, parent);
			}
			joinMap.put(path, join);
		}
		return join;
	}

	public void putJoin(MetaAttributePath path, F root) {
		if (joinMap.containsKey(path)) {
			throw new IllegalArgumentException(path.toString() + " already exists");
		}
		joinMap.put(path, root);
	}
}
