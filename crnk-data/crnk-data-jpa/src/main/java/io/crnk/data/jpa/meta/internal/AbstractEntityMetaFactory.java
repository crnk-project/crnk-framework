package io.crnk.data.jpa.meta.internal;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.data.jpa.meta.MetaEntityAttribute;
import io.crnk.data.jpa.meta.MetaJpaDataObject;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimaryKey;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractEntityMetaFactory<T extends MetaJpaDataObject> extends AbstractJpaDataObjectFactory<T> {

	@Override
	public MetaElement create(Type type) {
		Class<?> rawClazz = ClassUtils.getRawType(type);
		Class<?> superClazz = rawClazz.getSuperclass();
		MetaElement superMeta = getSuperMeta(superClazz);

		T meta = newDataObject();
		meta.setElementType(meta);
		meta.setName(rawClazz.getSimpleName());
		meta.setImplementationType(type);
		meta.setSuperType((MetaDataObject) superMeta);
		if (superMeta != null) {
			((MetaDataObject) superMeta).addSubType(meta);
		}
		createAttributes(meta);

		setKey(meta);

		return meta;
	}

	@Override
	protected String getMetaName(BeanAttributeInformation attrInformation) {
		return attrInformation.getName();
	}

	private MetaElement getSuperMeta(Class<?> superClazz) {
		if (superClazz.getAnnotation(Entity.class) != null || superClazz.getAnnotation(MappedSuperclass.class) != null) {
			return context.allocate(superClazz);
		}
		return null;
	}

	private void setKey(T meta) {
		if (meta.getPrimaryKey() == null) {
			boolean generated = false;
			ArrayList<MetaAttribute> pkElements = new ArrayList<>();
			for (MetaAttribute attr : meta.getAttributes()) {
				if (attr.getAnnotation(Id.class) != null || attr.getAnnotation(EmbeddedId.class) != null) {
					pkElements.add(attr);
					attr.setPrimaryKeyAttribute(true);

					boolean attrGenerated = attr.getAnnotation(GeneratedValue.class) != null;
					if (pkElements.size() == 1) {
						generated = attrGenerated;
					} else if (generated != attrGenerated) {
						throw new IllegalStateException(
								"cannot mix generated and not-generated primary key elements for " + meta.getId());
					}
				}
			}
			if (!pkElements.isEmpty()) {
				MetaPrimaryKey primaryKey = new MetaPrimaryKey();
				primaryKey.setName(meta.getName() + "$primaryKey");
				primaryKey.setElements(pkElements);
				primaryKey.setUnique(true);
				primaryKey.setParent(meta, true);
				primaryKey.setGenerated(generated);
				meta.setPrimaryKey(primaryKey);

				if (pkElements.size() == 1) {
					// single pk element cannot be nullable
					MetaAttribute pkElement = pkElements.get(0);
					pkElement.setNullable(false);
				}
			}
		}
	}

	protected abstract T newDataObject();

	@Override
	protected MetaAttribute createAttribute(T metaDataObject, String name) {
		MetaEntityAttribute attr = new MetaEntityAttribute();
		attr.setName(name);
		attr.setParent(metaDataObject, true);
		return attr;
	}

	@Override
	protected void initAttribute(MetaAttribute attr) {
		ManyToMany manyManyAnnotation = attr.getAnnotation(ManyToMany.class);
		ManyToOne manyOneAnnotation = attr.getAnnotation(ManyToOne.class);
		OneToMany oneManyAnnotation = attr.getAnnotation(OneToMany.class);
		OneToOne oneOneAnnotation = attr.getAnnotation(OneToOne.class);
		Version versionAnnotation = attr.getAnnotation(Version.class);
		Lob lobAnnotation = attr.getAnnotation(Lob.class);
		Column columnAnnotation = attr.getAnnotation(Column.class);

		boolean idAttr = attr.getAnnotation(Id.class) != null || attr.getAnnotation(EmbeddedId.class) != null;
		boolean attrGenerated = attr.getAnnotation(GeneratedValue.class) != null;

		attr.setVersion(versionAnnotation != null);
		attr.setAssociation(
				manyManyAnnotation != null || manyOneAnnotation != null || oneManyAnnotation != null || oneOneAnnotation !=
						null);

		attr.setLazy(isJpaLazy(attr.getAnnotations(), attr.isAssociation()));
		attr.setLob(lobAnnotation != null);
		attr.setFilterable(lobAnnotation == null);
		attr.setSortable(lobAnnotation == null);

		attr.setCascaded(getCascade(manyManyAnnotation, manyOneAnnotation, oneManyAnnotation, oneOneAnnotation));

		if (attr.getReadMethod() == null) {
			throw new IllegalStateException("no getter found for " + attr.getParent().getName() + "." + attr.getName());
		}
		Class<?> attributeType = attr.getReadMethod().getReturnType();
		boolean isPrimitiveType = ClassUtils.isPrimitiveType(attributeType);
		boolean columnNullable = (columnAnnotation == null || columnAnnotation.nullable()) &&
				(manyOneAnnotation == null || manyOneAnnotation.optional()) &&
				(oneOneAnnotation == null || oneOneAnnotation.optional());
		attr.setNullable(!isPrimitiveType && columnNullable);

		boolean hasSetter = attr.getWriteMethod() != null;
		attr.setInsertable(hasSetter && (columnAnnotation == null || columnAnnotation.insertable()) && !attrGenerated
				&& versionAnnotation == null);
		attr.setUpdatable(
				hasSetter && (columnAnnotation == null || columnAnnotation.updatable()) && !idAttr && versionAnnotation == null);

	}

	protected boolean isJpaLazy(Collection<Annotation> annotations, boolean isAssociation) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof OneToMany) {
				OneToMany oneToMany = (OneToMany) annotation;
				return oneToMany.fetch() == FetchType.LAZY;
			}
			if (annotation instanceof ManyToOne) {
				ManyToOne manyToOne = (ManyToOne) annotation;
				return manyToOne.fetch() == FetchType.LAZY;
			}
			if (annotation instanceof ManyToMany) {
				ManyToMany manyToMany = (ManyToMany) annotation;
				return manyToMany.fetch() == FetchType.LAZY;
			}
			if (annotation instanceof ElementCollection) {
				ElementCollection elementCollection = (ElementCollection) annotation;
				return elementCollection.fetch() == FetchType.LAZY;
			}
		}
		return isAssociation;
	}


	private boolean getCascade(ManyToMany manyManyAnnotation, ManyToOne manyOneAnnotation, OneToMany oneManyAnnotation,
							   OneToOne oneOneAnnotation) {
		if (manyManyAnnotation != null) {
			return getCascade(manyManyAnnotation.cascade());
		}
		if (manyOneAnnotation != null) {
			return getCascade(manyOneAnnotation.cascade());
		}
		if (oneManyAnnotation != null) {
			return getCascade(oneManyAnnotation.cascade());
		}
		if (oneOneAnnotation != null) {
			return getCascade(oneOneAnnotation.cascade());
		}
		return false;
	}

	private boolean getCascade(CascadeType[] types) {
		return types.length > 0;
	}

}