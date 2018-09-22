package io.crnk.gen.typescript.model;

import java.util.HashMap;
import java.util.Map;

public abstract class TSElementBase implements TSElement {

	private TSElement parent;

	private Map<String, Object> privateData = new HashMap<>();

	@Override
	public TSElement getParent() {
		return parent;
	}

	@Override
	public void setParent(TSElement parent) {
		this.parent = parent;
	}


	@Override
	public TSType asType() {
		throw new UnsupportedOperationException("not a type");
	}

	@SuppressWarnings("unchecked")
	public <T> T getPrivateData(String key, Class<T> type) {
		return (T) privateData.get(key);
	}

	public void setPrivateData(String key, Object value) {
		privateData.put(key, value);
	}


}
