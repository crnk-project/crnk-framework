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
	public void setUp() throws Exception {
		fieldA = new ResourceFieldImpl("a", "a", ResourceFieldType.ATTRIBUTE, String.class, String.class, null);
		fieldB = new ResourceFieldImpl("b", "b", ResourceFieldType.ATTRIBUTE, String.class, String.class, null);
	}

	@Test
	public void onTwoFieldsShouldSortCorrectly() throws Exception {
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
	public void onOneFieldShouldSortCorrectly() throws Exception {
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
	public void onNoOrderShouldPersistInsertionOrder() throws Exception {
		// GIVEN
		Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{}, false));

		// WHEN
		fields.add(fieldB);
		fields.add(fieldA);

		// THEN
		assertThat(fields).containsSequence(fieldB, fieldA);
	}

	@Test
	public void onAlphabeticOrderShouldSortCorrectly() throws Exception {
		// GIVEN
		Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{}, true));

		// WHEN
		fields.add(fieldB);
		fields.add(fieldA);

		// THEN
		assertThat(fields).containsSequence(fieldA, fieldB);
	}
}
