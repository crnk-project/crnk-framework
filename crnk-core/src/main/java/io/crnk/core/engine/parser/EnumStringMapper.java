package io.crnk.core.engine.parser;

public class EnumStringMapper<T> extends ToStringStringMapper<T> {

	private final Class<T> clazz;

	public EnumStringMapper(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public T parse(String input) {
		return (T) Enum.valueOf((Class<Enum>) clazz.asSubclass(Enum.class), input.trim());
	}
}
