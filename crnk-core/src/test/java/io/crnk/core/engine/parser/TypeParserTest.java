package io.crnk.core.engine.parser;

import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeParserTest {

	private final TypeParser sut = new TypeParser();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onStringShouldReturnString() throws Exception {
		String result = sut.parse("String", String.class);
		assertThat(result).isExactlyInstanceOf(String.class);
		assertThat(result).isEqualTo("String");
	}

	@Test(expected = ParserException.class)
	public void onInvalidCharacterThrowException() throws Exception {
		sut.parse("NOT a single character", Character.class);
	}

	@Test(expected = ParserException.class)
	public void onInvalidBooleanThrowException() throws Exception {
		sut.parse("NOT a boolean", Character.class);
	}

	@Test
	public void onBooleanFReturnFalse() throws Exception {
		Assert.assertFalse(sut.parse("f", Boolean.class));
	}

	@Test
	public void onNullStringShouldReturnNullString() throws Exception {
		String result = sut.parse((String) null, String.class);
		assertThat(result).isNull();
	}

	@Test
	public void onCharacterShouldReturnCharacter() throws Exception {
		Character result = sut.parse("a", Character.class);
		assertThat(result).isExactlyInstanceOf(Character.class);
		assertThat(result).isEqualTo('a');
	}

	@Test
	public void onCharacterPrimitiveShouldReturnCharacter() throws Exception {
		Character result = sut.parse("a", char.class);
		assertThat(result).isExactlyInstanceOf(Character.class);
		assertThat(result).isEqualTo('a');
	}

	@Test
	public void onLongCharacterShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(ParserException.class);

		// WHEN
		sut.parse("ab", Character.class);
	}

	@Test
	public void onUUIDStringShouldReturnUUID() throws Exception {
		UUID result = sut.parse("de305d54-75b4-431b-adb2-eb6b9e546014", UUID.class);
		assertThat(result).isExactlyInstanceOf(UUID.class);
		assertThat(result).isEqualTo(UUID.fromString("de305d54-75b4-431b-adb2-eb6b9e546014"));
	}

	@Test(expected = ParserException.class)
	public void onInvalidUUIDStringShouldThrowParserException() throws Exception {
		sut.parse("invalid", UUID.class);
	}

	@Test
	public void onBooleanTrueShouldReturnBoolean() throws Exception {
		Boolean result = sut.parse("true", Boolean.class);
		assertThat(result).isExactlyInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onBooleanTShouldReturnBoolean() throws Exception {
		Boolean result = sut.parse("t", Boolean.class);
		assertThat(result).isExactlyInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onBooleanFalseShouldReturnBoolean() throws Exception {
		Boolean result = sut.parse("false", Boolean.class);
		assertThat(result).isExactlyInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);
	}

	@Test
	public void onBooleanFShouldReturnBoolean() throws Exception {
		Boolean result = sut.parse("f", Boolean.class);
		assertThat(result).isExactlyInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);
	}

	@Test
	public void onBadBooleanShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(ParserException.class);

		// WHEN
		sut.parse("ab", Boolean.class);
	}

	@Test
	public void onByteShouldReturnByte() throws Exception {
		Byte result = sut.parse("1", Byte.class);
		assertThat(result).isExactlyInstanceOf(Byte.class);
		assertThat(result).isEqualTo((byte) 1);
	}

	@Test
	public void onBytePrimitiveShouldReturnByte() throws Exception {
		Byte result = sut.parse("1", byte.class);
		assertThat(result).isExactlyInstanceOf(Byte.class);
		assertThat(result).isEqualTo((byte) 1);
	}

	@Test
	public void onShortShouldReturnShort() throws Exception {
		Short result = sut.parse("1", Short.class);
		assertThat(result).isExactlyInstanceOf(Short.class);
		assertThat(result).isEqualTo((short) 1);
	}

	@Test
	public void onShortPrimitiveShouldReturnShort() throws Exception {
		Short result = sut.parse("1", short.class);
		assertThat(result).isExactlyInstanceOf(Short.class);
		assertThat(result).isEqualTo((short) 1);
	}

	@Test
	public void onIntegerShouldReturnInteger() throws Exception {
		Integer result = sut.parse("1", Integer.class);
		assertThat(result).isExactlyInstanceOf(Integer.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	public void onIntegerPrimitiveShouldReturnInteger() throws Exception {
		Integer result = sut.parse("1", int.class);
		assertThat(result).isExactlyInstanceOf(Integer.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	public void onLongShouldReturnLong() throws Exception {
		Long result = sut.parse("1", Long.class);
		assertThat(result).isExactlyInstanceOf(Long.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	public void onLongPrimitiveShouldReturnLong() throws Exception {
		Long result = sut.parse("1", long.class);
		assertThat(result).isExactlyInstanceOf(Long.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	public void onFloatShouldReturnFloat() throws Exception {
		Float result = sut.parse("1", Float.class);
		assertThat(result).isExactlyInstanceOf(Float.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	public void onFloatPrimitiveShouldReturnFloat() throws Exception {
		Float result = sut.parse("1", float.class);
		assertThat(result).isExactlyInstanceOf(Float.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	public void onDoubleShouldReturnDouble() throws Exception {
		Double result = sut.parse("1", Double.class);
		assertThat(result).isExactlyInstanceOf(Double.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	public void onDoublePrimitiveShouldReturnDouble() throws Exception {
		Double result = sut.parse("1", double.class);
		assertThat(result).isExactlyInstanceOf(Double.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	public void onBigIntegerShouldReturnBigInteger() throws Exception {
		BigInteger result = sut.parse("1", BigInteger.class);
		assertThat(result).isExactlyInstanceOf(BigInteger.class);
		assertThat(result).isEqualTo(new BigInteger("1"));
	}

	@Test
	public void onBigDecimalShouldReturnBigDecimal() throws Exception {
		BigDecimal result = sut.parse("1", BigDecimal.class);
		assertThat(result).isExactlyInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal("1"));
	}

	@Test
	public void onEnumShouldReturnEnumValue() throws Exception {
		SampleEnum result = sut.parse("SAMPLE_VALUE", SampleEnum.class);
		assertThat(result).isExactlyInstanceOf(SampleEnum.class);
		assertThat(result).isEqualTo(SampleEnum.SAMPLE_VALUE);
	}

	@Test
	public void onClassWithStringConstructorShouldReturnClassInstance() throws Exception {
		SampleClass result = sut.parse("input", SampleClass.class);
		assertThat(result).isExactlyInstanceOf(SampleClass.class);
		assertThat(result).isEqualTo(new SampleClass("input"));
	}

	@Test
	public void hasUtilsHavePrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(DefaultStringParsers.class);
	}

	@Test
	public void onUnknownClassShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(ParserException.class);

		// WHEN
		sut.parse("input", UnknownClass.class);
	}

	@Test
	public void testAddParser() throws Exception {
		sut.addParser(Boolean.class, new StringParser<Boolean>() {
			@Override
			public Boolean parse(String input) {
				return true;
			}
		});

		Assert.assertTrue(sut.parse("input", Boolean.class));
	}

	@Test
	public void onListOfLongsShouldReturnListOfLongs() throws Exception {
		Iterable<Long> result = sut.parse(Collections.singletonList("1"), Long.class);
		assertThat(result).hasSize(1);
		assertThat(result.iterator().next()).isEqualTo(1L);
	}


	@Test
	public void shouldMakeUseofParseCharSequenceMethod() throws Exception {
		StaticParseCharSequenceClass result = sut.parse("1", StaticParseCharSequenceClass.class);
		assertThat(result).isExactlyInstanceOf(StaticParseCharSequenceClass.class);
		assertThat(result.toString()).isEqualTo("1");
	}

	@Test
	public void shouldMakeUseofParseStringMethod() throws Exception {
		StaticParseStringClass result = sut.parse("1", StaticParseStringClass.class);
		assertThat(result).isExactlyInstanceOf(StaticParseStringClass.class);
		assertThat(result.toString()).isEqualTo("1");
	}


	// CharSequenceParseClass


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