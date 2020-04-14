package io.crnk.core.engine.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeParserTest {

	private ObjectMapper mapper;

	private TypeParser sut;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		sut = new TypeParser();
		mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.registerModule(new JavaTimeModule());
		sut.setObjectMapper(mapper);
	}

	@Test
	public void checkOffsetDateTime() {
		String text = "2018-09-23T16:12:43.154Z";
		OffsetDateTime value = sut.parse(text, OffsetDateTime.class);
		assertThat(sut.toString(value)).isEqualTo(text);
		assertThat(sut.getMapper(OffsetDateTime.class)).isInstanceOf(JacksonStringMapper.class);
	}


	@Test
	public void onStringShouldReturnString() {
		String result = sut.parse("String", String.class);
		assertThat(result).isExactlyInstanceOf(String.class);
		assertThat(result).isEqualTo("String");

		assertThat(sut.toString(result)).isEqualTo(result);
	}

	@Test(expected = ParserException.class)
	public void onInvalidCharacterThrowException() {
		sut.parse("NOT a single character", Character.class);
	}

	@Test(expected = ParserException.class)
	public void onInvalidBooleanThrowException() {
		sut.parse("NOT a boolean", Character.class);
	}

	@Test
	public void onBooleanFReturnFalse() {
		Assert.assertFalse(sut.parse("f", Boolean.class));
	}

	@Test
	public void checkBooleanToString() {
		assertThat(sut.toString(Boolean.FALSE)).isEqualTo("false");
		assertThat(sut.toString(Boolean.TRUE)).isEqualTo("true");
	}

	@Test
	public void checkNullToString() {
		assertThat(sut.toString(null)).isNull();
	}

	@Test
	public void onNullStringShouldReturnNullString() {
		String result = sut.parse((String) null, String.class);
		assertThat(result).isNull();
	}

	@Test
	public void onCharacterShouldReturnCharacter() {
		Character result = sut.parse("a", Character.class);
		assertThat(result).isExactlyInstanceOf(Character.class);
		assertThat(result).isEqualTo('a');
	}

	@Test
	public void onCharacterPrimitiveShouldReturnCharacter() {
		Character result = sut.parse("a", char.class);
		assertThat(result).isExactlyInstanceOf(Character.class);
		assertThat(result).isEqualTo('a');
	}

	@Test
	public void onLongCharacterShouldThrowException() {
		// THEN
		expectedException.expect(ParserException.class);

		// WHEN
		sut.parse("ab", Character.class);
	}

	@Test
	public void onUUIDStringShouldReturnUUID() {
		UUID result = sut.parse("de305d54-75b4-431b-adb2-eb6b9e546014", UUID.class);
		assertThat(result).isExactlyInstanceOf(UUID.class);
		assertThat(result).isEqualTo(UUID.fromString("de305d54-75b4-431b-adb2-eb6b9e546014"));
	}

	@Test(expected = ParserException.class)
	public void onInvalidUUIDStringShouldThrowParserException() {
		sut.parse("invalid", UUID.class);
	}

	@Test
	public void onBooleanTrueShouldReturnBoolean() {
		Boolean result = sut.parse("true", Boolean.class);
		assertThat(result).isExactlyInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onBooleanTShouldReturnBoolean() {
		Boolean result = sut.parse("t", Boolean.class);
		assertThat(result).isExactlyInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onBooleanFalseShouldReturnBoolean() {
		Boolean result = sut.parse("false", Boolean.class);
		assertThat(result).isExactlyInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);
	}

	@Test
	public void onBooleanFShouldReturnBoolean() {
		Boolean result = sut.parse("f", Boolean.class);
		assertThat(result).isExactlyInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);
	}

	@Test
	public void onBadBooleanShouldThrowException() {
		// THEN
		expectedException.expect(ParserException.class);

		// WHEN
		sut.parse("ab", Boolean.class);
	}

	@Test
	public void onByteShouldReturnByte() {
		Byte result = sut.parse("1", Byte.class);
		assertThat(result).isExactlyInstanceOf(Byte.class);
		assertThat(result).isEqualTo((byte) 1);
	}

	@Test
	public void onBytePrimitiveShouldReturnByte() {
		Byte result = sut.parse("1", byte.class);
		assertThat(result).isExactlyInstanceOf(Byte.class);
		assertThat(result).isEqualTo((byte) 1);
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onShortShouldReturnShort() {
		Short result = sut.parse("1", Short.class);
		assertThat(result).isExactlyInstanceOf(Short.class);
		assertThat(result).isEqualTo((short) 1);
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onShortPrimitiveShouldReturnShort() {
		Short result = sut.parse("1", short.class);
		assertThat(result).isExactlyInstanceOf(Short.class);
		assertThat(result).isEqualTo((short) 1);
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onIntegerShouldReturnInteger() {
		Integer result = sut.parse("1", Integer.class);
		assertThat(result).isExactlyInstanceOf(Integer.class);
		assertThat(result).isEqualTo(1);
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onIntegerPrimitiveShouldReturnInteger() {
		Integer result = sut.parse("1", int.class);
		assertThat(result).isExactlyInstanceOf(Integer.class);
		assertThat(result).isEqualTo(1);
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onLongShouldReturnLong() {
		Long result = sut.parse("1", Long.class);
		assertThat(result).isExactlyInstanceOf(Long.class);
		assertThat(result).isEqualTo(1);
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onLongPrimitiveShouldReturnLong() {
		Long result = sut.parse("1", long.class);
		assertThat(result).isExactlyInstanceOf(Long.class);
		assertThat(result).isEqualTo(1);
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onFloatShouldReturnFloat() {
		Float result = sut.parse("1", Float.class);
		assertThat(result).isExactlyInstanceOf(Float.class);
		assertThat(result).isEqualTo(1);
		assertThat(sut.toString(result)).isEqualTo("1.0");
	}

	@Test
	public void onFloatPrimitiveShouldReturnFloat() {
		Float result = sut.parse("1", float.class);
		assertThat(result).isExactlyInstanceOf(Float.class);
		assertThat(result).isEqualTo(1);
		assertThat(sut.toString(result)).isEqualTo("1.0");
	}

	@Test
	public void onDoubleShouldReturnDouble() {
		Double result = sut.parse("1", Double.class);
		assertThat(result).isExactlyInstanceOf(Double.class);
		assertThat(result).isEqualTo(1);
		assertThat(sut.toString(result)).isEqualTo("1.0");
	}

	@Test
	public void onDoublePrimitiveShouldReturnDouble() {
		Double result = sut.parse("1", double.class);
		assertThat(result).isExactlyInstanceOf(Double.class);
		assertThat(result).isEqualTo(1);
		assertThat(sut.toString(result)).isEqualTo("1.0");
	}

	@Test
	public void onBigIntegerShouldReturnBigInteger() {
		BigInteger result = sut.parse("1", BigInteger.class);
		assertThat(result).isExactlyInstanceOf(BigInteger.class);
		assertThat(result).isEqualTo(new BigInteger("1"));
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onBigDecimalShouldReturnBigDecimal() {
		BigDecimal result = sut.parse("1", BigDecimal.class);
		assertThat(result).isExactlyInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal("1"));
		assertThat(sut.toString(result)).isEqualTo("1");
	}

	@Test
	public void onEnumShouldReturnEnumValue() {
		SampleEnum result = sut.parse("SAMPLE_VALUE", SampleEnum.class);
		assertThat(result).isExactlyInstanceOf(SampleEnum.class);
		assertThat(result).isEqualTo(SampleEnum.SAMPLE_VALUE);
		assertThat(sut.toString(result)).isEqualTo("SAMPLE_VALUE");
	}

	@Test
	public void onInvalidEnumShouldThrowParserException() {
		// THEN
		expectedException.expect(ParserException.class);

		// WHEN
		sut.parse("INVALID_SAMPLE_VALUE", SampleEnum.class);
	}

	@Test
	public void onClassWithStringConstructorShouldReturnClassInstance() {
		SampleClass result = sut.parse("input", SampleClass.class);
		assertThat(result).isExactlyInstanceOf(SampleClass.class);
		assertThat(result).isEqualTo(new SampleClass("input"));
	}

	@Test
	public void hasUtilsHavePrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(DefaultStringParsers.class);
	}

	@Test
	public void onUnknownClassShouldThrowException() {
		// THEN
		expectedException.expect(ParserException.class);

		// WHEN
		sut.parse("input", UnknownClass.class);
	}

	@Test
	public void testAddParser() {
		sut.addParser(Boolean.class, new StringParser<Boolean>() {
			@Override
			public Boolean parse(String input) {
				return true;
			}
		});

		Assert.assertTrue(sut.parse("input", Boolean.class));
	}

	@Test
	public void onListOfLongsShouldReturnListOfLongs() {
		Iterable<Long> result = sut.parse(Collections.singletonList("1"), Long.class);
		assertThat(result).hasSize(1);
		assertThat(result.iterator().next()).isEqualTo(1L);
	}


	@Test
	public void shouldMakeUseofParseCharSequenceMethod() {
		StaticParseCharSequenceClass result = sut.parse("1", StaticParseCharSequenceClass.class);
		assertThat(result).isExactlyInstanceOf(StaticParseCharSequenceClass.class);
		assertThat(result.toString()).isEqualTo("1");
	}

	@Test
	public void shouldMakeUseofParseStringMethod() {
		StaticParseStringClass result = sut.parse("1", StaticParseStringClass.class);
		assertThat(result).isExactlyInstanceOf(StaticParseStringClass.class);
		assertThat(result.toString()).isEqualTo("1");
	}


	@Test
	public void localDateShouldBeHandledByJackson() throws Exception {
		JavaTimeModule module = new JavaTimeModule();
		mapper.registerModule(module);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		LocalDateTime dateValue = LocalDateTime.now();

		String jsonValue = mapper.writerFor(LocalDateTime.class).writeValueAsString(dateValue);
		String stringValue = jsonValue.substring(1, jsonValue.length() - 1);

		LocalDateTime parsedValue = sut.parse(stringValue, LocalDateTime.class);
		Assert.assertEquals(dateValue, parsedValue);

		StringParser<LocalDateTime> parser = sut.getParser(LocalDateTime.class);
		Assert.assertTrue(parser instanceof JacksonStringMapper);
	}

	private enum SampleEnum {
		SAMPLE_VALUE
	}

	public static class SampleClass implements Serializable {

		private final String input;

		public SampleClass(@SuppressWarnings("SameParameterValue") String input) {
			this.input = input;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof SampleClass)) {
				return false;
			}
			SampleClass that = (SampleClass) o;
			return Objects.equals(input, that.input);
		}

		@Override
		public int hashCode() {
			return Objects.hash(input);
		}
	}

	public static class StaticParseStringClass implements Serializable {

		private final String input;

		private StaticParseStringClass(String input) {
			this.input = input;
		}

		public static StaticParseStringClass parse(String input) {
			return new StaticParseStringClass(input);
		}

		public String toString() {
			return input;
		}
	}

	public static class StaticParseCharSequenceClass implements Serializable {

		private final String input;

		private StaticParseCharSequenceClass(String input) {
			this.input = input;
		}

		public static StaticParseCharSequenceClass parse(CharSequence input) {
			return new StaticParseCharSequenceClass(input.toString());
		}

		public String toString() {
			return input;
		}
	}

	private static class UnknownClass implements Serializable {

	}
}