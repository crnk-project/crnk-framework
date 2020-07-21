package io.crnk.core.engine.information.bean;

import io.crnk.core.engine.internal.utils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanInformation {

	private final Class implementationClass;

	private Map<String, BeanAttributeInformation> attributeMap = new HashMap<>();

	private Map<String, BeanAttributeInformation> jsonAttributeMap = new HashMap<>();

	private List<String> attributeNames = new ArrayList<>();

	private BeanInformation superType;

	private List<BeanInformation> implementedInterfaces = new ArrayList<>();

	private BeanInformation(Class implementationClass) {
		this.implementationClass = implementationClass;

		List<Field> fields = ClassUtils.getClassFields(implementationClass);
		List<Method> getters = ClassUtils.getClassGetters(implementationClass);

		for (Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers())) {
				BeanAttributeInformation attrDesc = new BeanAttributeInformation(this, field);
				attributeMap.put(field.getName(), attrDesc);
				attributeNames.add(field.getName());
			}
		}

		for (Method getter : getters) {
			if (!Modifier.isStatic(getter.getModifiers())) {
				String name = ClassUtils.getGetterFieldName(getter);
				BeanAttributeInformation attrDesc = attributeMap.get(name);
				if (attrDesc == null) {
					attrDesc = new BeanAttributeInformation(this, getter, name);
					attributeMap.put(name, attrDesc);
					attributeNames.add(name);
				} else {
					attrDesc.setGetter(getter);
				}
			}
		}

		Iterator<String> iterator = attributeNames.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			BeanAttributeInformation attributeInformation = attributeMap.get(name);
			Field field = attributeInformation.getField();
			Method getter = attributeInformation.getGetter();
			if ((field == null || !Modifier.isPublic(field.getModifiers())) && (getter == null || !Modifier
					.isPublic(getter.getModifiers()))) {
				// no public accessor
				iterator.remove();
				attributeMap.remove(name);
			}
		}

		for (BeanAttributeInformation attrDesc : attributeMap.values()) {
			String name = attrDesc.getName();
			Class<?> attrType = attrDesc.getImplementationClass();
			attrDesc.setSetter(ClassUtils.findSetter(implementationClass, name, attrType));
			jsonAttributeMap.put(attrDesc.getJsonName(), attrDesc);
		}

		if (implementationClass.getSuperclass() != null && implementationClass.getSuperclass() != Object.class) {
			superType = new BeanInformation(implementationClass.getSuperclass());
		}

		for (Class<?> interfaceClass : implementationClass.getInterfaces()) {
			implementedInterfaces.add(new BeanInformation(interfaceClass));
		}
	}

	public BeanInformation getSuperType() {
		return superType;
	}

	public List<BeanInformation> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	public BeanAttributeInformation getAttribute(String name) {
		return attributeMap.get(name);
	}

	public Collection<BeanAttributeInformation> getAttributes() {
		return attributeMap.values();
	}

	public BeanAttributeInformation getAttributeByJsonName(String jsonName) {
		return jsonAttributeMap.get(jsonName);
	}

	public List<String> getAttributeNames() {
		return attributeNames;
	}

	public Class getImplementationClass() {
		return implementationClass;
	}

	private static final ConcurrentHashMap<Class, BeanInformation> cache = new ConcurrentHashMap<>();

	public static BeanInformation get(Class<?> clazz) {
		return cache.computeIfAbsent(clazz, BeanInformation::new);
	}
}
