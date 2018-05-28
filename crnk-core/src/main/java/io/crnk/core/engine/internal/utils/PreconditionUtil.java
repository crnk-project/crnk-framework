package io.crnk.core.engine.internal.utils;

public class PreconditionUtil {

	/**
	 * private constructor since it is a static only class
	 */
	private PreconditionUtil() {
	}

	/**
	 * Asserts that two objects are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If
	 * <code>expected</code> and <code>actual</code> are <code>null</code>, they
	 * are considered equal.
	 *
	 * @param message  the identifying message for the {@link AssertionError} (
	 *                 <code>null</code> okay)
	 * @param expected expected value
	 * @param actual   actual value
	 */
	public static void assertEquals(String message, Object expected, Object actual) {
		verifyEquals(expected, actual, message);
	}

	public static void verifyEquals(Object expected, Object actual, String message, Object... args) {
		if (!CompareUtils.isEquals(expected, actual)) {
			fail(format(message, args, expected, actual));
		}
	}

	static String format(String message, Object[] args, Object expected, Object actual) {
		String formatted = "";
		if (message != null && !message.equals("")) {
			formatted = String.format(message, args) + " ";
		}
		String expectedString = String.valueOf(expected);
		String actualString = String.valueOf(actual);
		return formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
	}

	/**
	 * Fails a test with the given message.
	 *
	 * @param message the identifying message for the {@link AssertionError} (
	 *                <code>null</code> okay)
	 * @see AssertionError
	 */
	public static void fail(String message, Object... args) {
		throw new IllegalStateException(message == null ? "" : String.format(message, args));
	}

	/**
	 * Asserts that an object isn't null. If it is an {@link AssertionError} is
	 * thrown with the given message.
	 *
	 * @param message the identifying message for the {@link AssertionError} (
	 *                <code>null</code> okay)
	 * @param object  Object to check or <code>null</code>
	 */
	public static void assertNotNull(String message, Object object) {
		assertTrue(message, object != null);
	}

	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * {@link AssertionError} with the given message.
	 *
	 * @param message   the identifying message for the {@link AssertionError} (
	 *                  <code>null</code> okay)
	 * @param condition condition to be checked
	 */
	public static void assertTrue(String message, boolean condition) {
		verify(condition, message);
	}


	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * {@link AssertionError} with the given message.
	 *
	 * @param message   the identifying message for the {@link AssertionError} (
	 *                  <code>null</code> okay)
	 * @param condition condition to be checked
	 */
	public static void assertTrue(boolean condition, String message, Object... args) {
		verify(condition, message, args);
	}


	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * {@link AssertionError} with the given message.
	 *
	 * @param message   the identifying message for the {@link AssertionError} (
	 *                  <code>null</code> okay)
	 * @param condition condition to be checked
	 */
	public static void assertFalse(boolean condition, String message, Object... args) {
		verify(!condition, message, args);
	}


	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * {@link AssertionError} with the given message.
	 *
	 * @param message   the identifying message for the {@link AssertionError} (
	 *                  <code>null</code> okay)
	 * @param condition condition to be checked
	 */
	public static void assertFalse(String message, boolean condition) {
		assertTrue(message, !condition);
	}

	/**
	 * Asserts that an object is null. If it is not, an {@link AssertionError}
	 * is thrown with the given message.
	 *
	 * @param message the identifying message for the {@link AssertionError} (
	 *                <code>null</code> okay)
	 * @param object  Object to check or <code>null</code>
	 */
	public static void assertNull(String message, Object object) {
		assertTrue(message, object == null);
	}

	public static void verify(boolean condition, String messageFormat, Object... args) {
		if (!condition) {
			String message = messageFormat != null ? String.format(messageFormat, args) : null;
			fail(message);
		}
	}
}
