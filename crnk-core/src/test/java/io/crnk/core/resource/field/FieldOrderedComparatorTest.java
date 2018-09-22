package io.crnk.core.resource.field;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.internal.utils.FieldOrderedComparator;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldOrderedComparatorTest {

	ResourceField fieldA;
	ResourceField fieldB;

	@Before
	public void setUp() {
		fieldA = new ResourceFieldImpl("a", "a", ResourceFieldType.ATTRIBUTE, String.class, String.class, null);
		fieldB = new ResourceFieldImpl("b", "b", ResourceFieldType.ATTRIBUTE, String.class, String.class, null);
	}

	@Test
	public void onTwoFieldsShouldSortCorrectly() {
		// GIVEN
		Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{
				"b", "a"
		}, false));

		// WHEN
		fields.add(fieldA);
		fields.add(fieldB);

		// THEN
		assertThat(fields).containsSequence(fieldB, fieldA);
	}

	@Test
	public void onOneFieldShouldSortCorrectly() {
		// GIVEN
		Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{
				"b"
		}, false));

		// WHEN
		fields.add(fieldA);
		fields.add(fieldB);

		// THEN
		assertThat(fields).containsSequence(fieldB, fieldA);
	}

	@Test
	public void onNoOrderShouldPersistInsertionOrder() {
		// GIVEN
		Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{}, false));

		// WHEN
		fields.add(fieldB);
		fields.add(fieldA);

		// THEN
		assertThat(fields).containsSequence(fieldB, fieldA);
	}

	@Test
	public void onAlphabeticOrderShouldSortCorrectly() {
		// GIVEN
		Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{}, true));

		// WHEN
		fields.add(fieldB);
		fields.add(fieldA);

		// THEN
		assertThat(fields).containsSequence(fieldA, fieldB);
	}
}
