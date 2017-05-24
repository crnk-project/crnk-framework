package io.crnk.meta.model;

import java.lang.annotation.Annotation;
import java.util.Collection;

import io.crnk.core.engine.parser.TypeParser;

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

	@Override
	public MetaType getType() {
		return mapType;
	}

	@Override
	public Object getValue(Object dataObject) {
		throw new UnsupportedOperationException();
	}

	@Override
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

	@Override
	public boolean isAssociation() {
		return mapAttr.isAssociation();
	}

	@Override
	public boolean isDerived() {
		return mapAttr.isDerived();
	}

	@Override
	public void addValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLazy() {
		return mapAttr.isLazy();
	}

	@Override
	public MetaAttribute getOppositeAttribute() {
		throw new UnsupportedOperationException();
	}

	public void setOppositeAttribute(MetaAttribute oppositeAttr) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVersion() {
		throw new UnsupportedOperationException();
	}

	public boolean isId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Annotation> getAnnotations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> clazz) {
		throw new UnsupportedOperationException();
	}
}
