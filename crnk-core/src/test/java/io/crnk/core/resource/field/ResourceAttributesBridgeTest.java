package io.crnk.core.resource.field;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.internal.information.resource.ReflectionFieldAccessor;
import io.crnk.core.engine.internal.information.resource.ResourceAttributesBridge;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.mock.models.Task;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class ResourceAttributesBridgeTest {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void onValidClassShouldInitializeResourceAttributesBridge() throws Exception {
		// WHEN
		new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), DynamicResource.class);
	}

	@Test(expected = InvalidResourceException.class)
	public void onClassWithoutAnyGetterShouldThrowException() throws Exception {
		// WHEN
		new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), ClassWithoutAnyGetter.class);
	}

	@Test(expected = InvalidResourceException.class)
	public void onClassWithoutAnySetterShouldThrowException() throws Exception {
		// WHEN
		new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), ClassWithoutAnySetter.class);
	}

	@Test
	public void onSimpleAttributesShouldPutInstanceValues() throws Exception {
		// GIVEN
		ResourceFieldImpl field = new ResourceFieldImpl("name", "name", ResourceFieldType.ATTRIBUTE, String.class, String.class, null);
		field.setAccessor(new ReflectionFieldAccessor(Task.class, "name", String.class));
		ResourceAttributesBridge<Task> sut =
				new ResourceAttributesBridge<>(Collections.singletonList((ResourceField) field), Task.class);

		JsonNode valueNode = objectMapper.readTree("\"value\"");

		Task task = new Task();

		// WHEN
		sut.setProperty(objectMapper, task, valueNode, "name");

		// THEN
		assertThat(task.getName()).isEqualTo("value");
	}

	@Test
	public void onDynamicAttributesShouldPutInstanceValues() throws Exception {
		// GIVEN
		ResourceAttributesBridge<DynamicResource> sut =
				new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), DynamicResource.class);
		DynamicResource resource = new DynamicResource();
		JsonNode valueNode = objectMapper.readTree("\"value\"");

		// WHEN
		sut.setProperty(objectMapper, resource, valueNode, "name");

		// THEN
		assertThat(resource.anyGetter())
				.containsOnly(entry("name", "value"));
	}

	@Test(expected = ResourceException.class)
	public void onDynamicAttributesReadingShouldThrowException() throws Exception {
		// GIVEN
		ResourceAttributesBridge<DynamicResourceWithSetterException> sut =
				new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), DynamicResourceWithSetterException.class);
		JsonNode valueNode = objectMapper.readTree("\"value\"");

		// WHEN
		sut.setProperty(objectMapper, new DynamicResourceWithSetterException(), valueNode, "name");
	}

	@Test(expected = ResourceException.class)
	public void onDynamicAttributesWritingShouldThrowException() throws Exception {
		// GIVEN
		ResourceAttributesBridge<DynamicResourceWithSetterException> sut =
				new ResourceAttributesBridge<>(Collections.<ResourceField>emptyList(), DynamicResourceWithSetterException.class);

		JsonNode valueNode = objectMapper.readTree("\"value\"");

		// WHEN
		sut.setProperty(objectMapper, new DynamicResourceWithSetterException(), valueNode, "name");
	}

	public static class DynamicResource {

		private Map<String, Object> values = new HashMap<>(1);

		@JsonAnyGetter
		public Map<String, Object> anyGetter() {
			return values;
		}

		@JsonAnySetter
		public void anySetter(String name, Object value) {
			values.put(name, value);
		}
	}

	public static class DynamicResourceWithSetterException {

		@JsonAnyGetter
		public Map<String, Object> anyGetter() {
			return Collections.emptyMap();
		}

		@JsonAnySetter
		public void anySetter(String name, Object value) {
			throw new IllegalStateException();
		}
	}

	public static class DynamicResourceWithGetterException {

		@JsonAnyGetter
		public Map<String, Object> anyGetter() {
			throw new IllegalStateException();
		}

		@JsonAnySetter
		public void anySetter(String name, Object value) {
		}
	}

	private static class ClassWithoutAnyGetter {
		@JsonAnySetter
		public void anySetter(String name, Object value) {
		}
	}

	private static class ClassWithoutAnySetter {
		@JsonAnyGetter
		public Map<String, Object> anyGetter() {
			return Collections.emptyMap();
		}
	}
}
