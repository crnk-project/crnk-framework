package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ForwardingStrategyBase {

	protected ForwardingStrategyContext context;

	public void init(ForwardingStrategyContext context) {
		PreconditionUtil.verify(this.context == null, "this strategy can only be initialized once");
		this.context = context;
	}

	protected <D> Collection<D> getOrCreateCollection(Object source, ResourceField field) {
		Object property = field.getAccessor().getValue(source);
		if (property == null) {
			Class<?> propertyClass = field.getType();
			boolean isList = List.class.isAssignableFrom(propertyClass);
			property = isList ? new ArrayList() : new HashSet();
			field.getAccessor().setValue(source, property);
		}
		return (Collection<D>) property;
	}
}
