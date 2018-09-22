package io.crnk.core.engine.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.crnk.core.engine.internal.utils.MethodCache;
import io.crnk.core.utils.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parses {@link String} into an instance of provided {@link Class}. It support
 * the following classes:
 * <ol>
 * <li>{@link String}</li>
 * <li>{@link Byte} and <i>byte</i></li>
 * <li>{@link Short} and <i>short</i></li>
 * <li>{@link Integer} and <i>int</i></li>
 * <li>{@link Long} and <i>long</i></li>
 * <li>{@link Float} and <i>float</i></li>
 * <li>{@link Double} and <i>double</i></li>
 * <li>{@link BigInteger}</li>
 * <li>{@link BigDecimal}</li>
 * <li>{@link Character} and <i>char</i></li>
 * <li>{@link Boolean} and <i>boolean</i></li>
 * <li>{@link java.util.UUID}</li>
 * <li>An {@link Enum}</li>
 * <li>A class with a {@link String} only constructor</li>
 * </ol>
 */
public class TypeParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypeParser.class);

	public final Map<Class, StringParser> parsers;

	private MethodCache methodCache = new MethodCache();

	private boolean useJackson = true;

	private ObjectMapper objectMapper;

	public TypeParser() {
		parsers = new ConcurrentHashMap<>();
		parsers.putAll(DefaultStringParsers.get());
	}

	public boolean isUseJackson() {
		return useJackson;
	}

	public void setUseJackson(boolean useJackson) {
		this.useJackson = useJackson;
	}

	private static <T> boolean isEnum(Class<T> clazz) {
		return clazz.isEnum();
	}

	/**
	 * Adds a custom parser for the given type.
	 */
	public <T> void addParser(Class<T> clazz, StringParser<T> parser) {
		parsers.put(clazz, parser);
	}

	/**
	 * Parses an {@link Iterable} of String instances to {@link Iterable} of
	 * parsed values.
	 *
	 * @param inputs list of Strings
	 * @param clazz  type to be parsed to
	 * @param <T>    type of class
	 * @return {@link Iterable} of parsed values
	 */
	public <T extends Serializable> Iterable<T> parse(Iterable<String> inputs, Class<T> clazz) {
		List<T> parsedValues = new LinkedList<>();
		for (String input : inputs) {
			parsedValues.add(parse(input, clazz));
		}

		return parsedValues;
	}

	/**
	 * Parses a {@link String} to an instance of passed {@link Class}
	 *
	 * @param input String value
	 * @param clazz type to be parsed to
	 * @param <T>   type of class
	 * @return instance of parsed value
	 */
	public <T> T parse(String input, Class<T> clazz) {
		try {
			return parseInput(input, clazz);
		} catch (ParserException e) {
			throw e;
		} catch (NumberFormatException e) {
			throw new ParserException(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T parseInput(final String input, final Class<T> clazz) {
		if (String.class.equals(clazz)) {
			return (T) input;
		}

		StringParser<T> parser = getParser(clazz, input);
		if (parser == null) {
			throw new ParserException(String.format("Cannot parse to %s : %s", clazz.getName(), input));
		}

		return (T) parser.parse(input);
	}

	private <T> StringParser<T> getParser(Class<T> clazz, String input) {
		if (parsers.containsKey(clazz)) {
			return parsers.get(clazz);

		}
		StringParser parser = setupParser(clazz, input);
		if (parser != null) {
			LOGGER.debug("using parser {} for type {}", parser, clazz);
			parsers.put(clazz, parser);
			return parser;
		}
		return null;
	}

	public boolean supports(Class<?> clazz) {
		return getParser(clazz, null) != null;
	}

	private <T> StringParser<T> setupParser(Class<T> clazz, String input) {
		if (parsers.containsKey(clazz)) {
			return parsers.get(clazz);
		}

		if (isEnum(clazz)) {
			return new EnumParser<>(clazz);
		}

		// TODO cleanup this input dependency
		if (useJackson && input != null) {
			ObjectReader reader = objectMapper.readerFor(clazz);
			try {
				JacksonParser parser = new JacksonParser(reader);
				parser.parse(input);
				return parser;
			} catch (RuntimeException e) {
				LOGGER.debug("Jackson not applicable to {} based on input {}", clazz, input);
				LOGGER.trace("Jackson error", e);
			}
		}

		try {
			if (containsStringConstructor(clazz)) {
				Constructor<T> constructor = clazz.getDeclaredConstructor(String.class);
				return new ConstructorBasedParser(constructor);
			}
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}

		Optional<Method> method = methodCache.find(clazz, "parse", String.class);
		if (!method.isPresent()) {
			method = methodCache.find(clazz, "parse", CharSequence.class);
		}
		if (method.isPresent()) {
			return new MethodBasedParser(method.get(), clazz);
		}

		return null;
	}

	private boolean containsStringConstructor(Class<?> clazz) {
		boolean result = false;
		for (Constructor constructor : clazz.getDeclaredConstructors()) {
			if (!Modifier.isPrivate(constructor.getModifiers()) && constructor.getParameterTypes().length == 1
					&& constructor.getParameterTypes()[0] == String.class) {
				result = true;
			}
		}
		return result;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T> StringParser<T> getParser(Class<T> clazz) {
		return parsers.get(clazz);
	}
}
