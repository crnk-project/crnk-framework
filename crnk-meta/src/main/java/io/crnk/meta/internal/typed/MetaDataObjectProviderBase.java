package io.crnk.meta.internal.typed;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.meta.internal.MetaUtils;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MetaDataObjectProviderBase<T extends MetaDataObject> implements TypedMetaElementFactory {


	protected TypedMetaElementFactoryContext context;

	public void init(TypedMetaElementFactoryContext context) {
		this.context = context;
	}

	protected void createAttributes(T meta) {
		Class<?> implClass = meta.getImplementationClass();

		BeanInformation beanInformation = BeanInformation.get(implClass);

		for (String name : beanInformation.getAttributeNames()) {
			BeanAttributeInformation attrInformation = beanInformation.getAttribute(name);
			if (attrInformation.getGetter() != null && !isIgnored(attrInformation)) {
				if (!attrInformation.isDeclaredHere() && !attrInformation.isConcretion()) {
					continue; // same information is contained in super type
				}

				String metaName = getMetaName(attrInformation);
				try {
					MetaAttribute attribute = createAttribute(meta, MetaUtils.firstToLower(metaName));
					attribute.setReadMethod(attrInformation.getGetter());
					attribute.setWriteMethod(attrInformation.getSetter());

					attribute.setSortable(true);
					attribute.setFilterable(true);
					if (attrInformation.getSetter() != null) {
						attribute.setInsertable(true);
						attribute.setUpdatable(true);
					}

					initAttribute(attribute);
				} catch (Exception e) {
					throw new IllegalStateException(
							"failed to create attribute " + implClass.getName() + "." + name + " with metaName=" + metaName, e);
				}
			}
		}
	}

	protected abstract String getMetaName(BeanAttributeInformation attrInformation);

	protected boolean isIgnored(BeanAttributeInformation information) {
		return false;
	}

	protected void initAttribute(MetaAttribute attribute) {
	}

	private List<String> getOrderedPropertyNames(List<Field> fields, List<Method> getters, Map<String, Field> fieldMap) {
		List<String> propertyNames = new ArrayList<>();
		for (Field field : fields) {
			propertyNames.add(field.getName());
		}
		for (Method method : getters) {
			String name = ClassUtils.getGetterFieldName(method);
			if (!fieldMap.containsKey(name)) {
				propertyNames.add(name);
			}
		}
		return propertyNames;
	}

	private Map<String, Field> toFieldMap(List<Field> members) {
		Map<String, Field> map = new HashMap<>();
		for (Field member : members) {
			map.put(member.getName(), member);
		}
		return map;
	}

	private Map<String, Method> toGetterMethodMap(List<Method> members) {
		Map<String, Method> map = new HashMap<>();
		for (Method member : members) {
			String name = ClassUtils.getGetterFieldName(member);
			map.put(name, member);
		}
		return map;
	}

	protected MetaAttribute createAttribute(T metaDataObject, String name) {
		MetaAttribute attr = new MetaAttribute();
		attr.setName(MetaUtils.firstToLower(name));
		attr.setParent(metaDataObject, true);
		attr.setFilterable(true);
		attr.setSortable(true);
		return attr;
	}
}
