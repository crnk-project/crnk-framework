package io.crnk.core.engine.parser;

public interface StringMapper<T> extends StringParser<T> {

	String toString(T input);

}
