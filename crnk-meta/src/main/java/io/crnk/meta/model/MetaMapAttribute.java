package io.crnk.meta.model;

import io.crnk.core.engine.parser.TypeParser;

import java.lang.annotation.Annotation;
import java.util.Collection;

@Deprecated
public class MetaMapAttribute extends MetaAttribute {

	private MetaMapType mapType;

	private String keyString;

	private MetaAttribute mapAttr;

	public MetaMapAttribute(MetaMapType mapType, MetaAttribute mapAttr, String keyString) {
		setName(mapAttr.getName());

		this.keyString = keyString;
		this.mapType = mapType;
		this.mapAttr = mapAttr;
	}

	@Override
	public MetaDataObject getParent() {
		return super.getParent();
	}

	public MetaType getType() {
		return mapType;
	}

	public Object getValue(Object dataObject) {
		throw new UnsupportedOperationException();
	}

	public void setValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

	public Object getKey() {
		MetaType keyType = mapType.getKeyType();
		TypeParser typeParser = new TypeParser();
		return typeParser.parse(keyString, (Class) keyType.getImplementationClass());
	}

	public MetaAttribute getMapAttribute() {
		return mapAttr;
	}

	public boolean isAssociation() {
		return mapAttr.isAssociation();
	}

	public boolean isDerived() {
		return mapAttr.isDerived();
	}

	public void addValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

	public void removeValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

	public boolean isLazy() {
		return mapAttr.isLazy();
	}

	public MetaAttribute getOppositeAttribute() {
		throw new UnsupportedOperationException();
	}

	public void setOppositeAttribute(MetaAttribute oppositeAttr) {
		throw new UnsupportedOperationException();
	}

	public boolean isVersion() {
		throw new UnsupportedOperationException();
	}

	public boolean isId() {
		throw new UnsupportedOperationException();
	}

	public Collection<Annotation> getAnnotations() {
		throw new UnsupportedOperationException();
	}

	public <T extends Annotation> T getAnnotation(Class<T> clazz) {
		throw new UnsupportedOperationException();
	}
}
