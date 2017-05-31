package io.crnk.core.engine.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Parsers for standard Java types.
 */
public final class DefaultStringParsers {

	private DefaultStringParsers() {
	}

	public static Map<Class, StringParser> get() {
		Map<Class, StringParser> parsers = new HashMap();

		addType(parsers, asList(Byte.class, byte.class), new StringParser<Byte>() {
			@Override
			public Byte parse(String input) {
				return Byte.valueOf(input);
			}
		});
		addType(parsers, asList(Short.class, short.class), new StringParser<Short>() {
			@Override
			public Short parse(String input) {
				return Short.valueOf(input);
			}
		});
		addType(parsers, asList(Integer.class, int.class), new StringParser<Integer>() {
			@Override
			public Integer parse(String input) {
				return Integer.valueOf(input);
			}
		});
		addType(parsers, asList(Long.class, long.class), new StringParser<Long>() {
			@Override
			public Long parse(String input) {
				return Long.valueOf(input);
			}
		});
		addType(parsers, asList(Float.class, float.class), new StringParser<Float>() {
			@Override
			public Float parse(String input) {
				return Float.valueOf(input);
			}
		});
		addType(parsers, asList(Double.class, double.class), new StringParser<Double>() {
			@Override
			public Double parse(String input) {
				return Double.valueOf(input);
			}
		});
		addType(parsers, singletonList(BigInteger.class), new StringParser<BigInteger>() {
			@Override
			public BigInteger parse(String input) {
				return new BigInteger(input);
			}
		});
		addType(parsers, singletonList(BigDecimal.class), new StringParser<BigDecimal>() {
			@Override
			public BigDecimal parse(String input) {
				return new BigDecimal(input);
			}
		});
		addType(parsers, asList(Character.class, char.class), new StringParser<Character>() {
			@Override
			public Character parse(String input) {
				if (input.length() != 1) {
					throw newException(Character.class, input);
				}
				return input.charAt(0);
			}
		});
		addType(parsers, asList(Boolean.class, boolean.class), new StringParser<Boolean>() {
			@Override
			public Boolean parse(String input) {
				String inputNormalized = input.toLowerCase();
				if ("true".equals(inputNormalized) || "t".equals(inputNormalized)) {
					return true;
				} else if ("false".equals(inputNormalized) || "f".equals(inputNormalized)) {
					return false;
				}
				throw newException(Boolean.class, input);
			}
		});
		addType(parsers, Collections.singletonList(UUID.class), new StringParser<UUID>() {
			@Override
			public UUID parse(String input) {
				try {
					return UUID.fromString(input);
				} catch (IllegalArgumentException e) {
					throw new ParserException("not a valid uuid", e);
				}
			}
		});

		return parsers;
	}

	private static <T> void addType(Map<Class, StringParser> parsers, List<Class<T>> classes, StringParser<T> standardTypeParser) {
		for (Class<T> clazz : classes) {
			parsers.put(clazz, standardTypeParser);
		}
	}

	private static ParserException newException(Class clazz, String input) {
		return new ParserException(String.format("String cannot be casted to %s: %s", clazz, input));
	}
}
