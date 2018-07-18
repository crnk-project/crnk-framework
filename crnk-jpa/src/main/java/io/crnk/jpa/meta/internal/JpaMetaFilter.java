package io.crnk.jpa.meta.internal;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.utils.Optional;
import io.crnk.jpa.meta.MetaJpaDataObject;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.provider.MetaFilterBase;
import io.crnk.meta.provider.MetaProviderContext;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.reflect.Type;

public class JpaMetaFilter extends MetaFilterBase {


	private final MetaProviderContext context;
	private final JpaMetaPartition partition;


	public JpaMetaFilter(JpaMetaPartition partition, MetaProviderContext context) {
		this.context = context;
		this.partition = partition;
	}

	@Override
	public void onInitialized(MetaElement element) {
		if (element.getParent() instanceof MetaJpaDataObject && element instanceof MetaAttribute) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaDataObject parent = attr.getParent();
			Type implementationType = PropertyUtils.getPropertyType(parent.getImplementationClass(), attr.getName());
			Optional<MetaElement> optMetaType = partition.allocateMetaElement(implementationType);
			PreconditionUtil.verify(optMetaType.isPresent(), "failed to read %s, make sure it is a properly annotated with JPA annotations", implementationType);
			MetaType metaType = (MetaType) optMetaType.get();
			attr.setType(metaType);
		}

		if (element.getParent() instanceof MetaJpaDataObject && element instanceof MetaAttribute
				&& ((MetaAttribute) element).getOppositeAttribute() == null) {
			MetaAttribute attr = (MetaAttribute) element;
			String mappedBy = getMappedBy(attr);
			if (mappedBy != null) {
				MetaType attrType = attr.getType();
				MetaDataObject oppositeType = attrType.getElementType().asDataObject();
				if (!mappedBy.contains(".")) {
					MetaAttribute oppositeAttr = oppositeType.getAttribute(mappedBy);
					attr.setOppositeAttribute(oppositeAttr);
				} else {
					// references within embeddables not yet supported
				}
			}
		}
	}


	private String getMappedBy(MetaAttribute attr) {
		ManyToMany manyManyAnnotation = attr.getAnnotation(ManyToMany.class);
		OneToMany oneManyAnnotation = attr.getAnnotation(OneToMany.class);
		OneToOne oneOneAnnotation = attr.getAnnotation(OneToOne.class);
		String mappedBy = null;
		if (manyManyAnnotation != null) {
			mappedBy = manyManyAnnotation.mappedBy();
		}
		if (oneManyAnnotation != null) {
			mappedBy = oneManyAnnotation.mappedBy();
		}
		if (oneOneAnnotation != null) {
			mappedBy = oneOneAnnotation.mappedBy();
		}

		if (mappedBy != null && mappedBy.length() == 0) {
			mappedBy = null;
		}
		return mappedBy;
	}
}
