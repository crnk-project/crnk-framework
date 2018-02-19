package io.crnk.core.resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpecDeserializer;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpecSerializer;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonApiRelationIdInformationProviderTest {

	private final ResourceInformationProvider resourceInformationProvider =
			new DefaultResourceInformationProvider(new NullPropertiesProvider(),
					new OffsetLimitPagingSpecSerializer(),
					new OffsetLimitPagingSpecDeserializer(),
					new DefaultResourceFieldInformationProvider(),
					new JacksonResourceFieldInformationProvider());

	private final ResourceInformationProviderContext context =
			new DefaultResourceInformationProviderContext(resourceInformationProvider,
					new DefaultInformationBuilder(new TypeParser()), new TypeParser(), new ObjectMapper());

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		resourceInformationProvider.init(context);
	}

	@Test
	public void shouldAutoDetectWithDefaultName() {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Schedule.class);
		ResourceField field = resourceInformation.findFieldByName("project");
		Assert.assertNotNull(field);
		Assert.assertNull(resourceInformation.findFieldByName("projectId"));

		Assert.assertTrue(field.hasIdField());

		Schedule schedule = new Schedule();
		Assert.assertNull(field.getIdAccessor().getValue(schedule));
		field.getIdAccessor().setValue(schedule, 13L);
		Assert.assertEquals(13L, schedule.getProjectId().longValue());
		Assert.assertEquals(13L, field.getIdAccessor().getValue(schedule));
	}

	@Test
	public void shouldAutoDetectWithCustomName() {
		ResourceInformation resourceInformation = resourceInformationProvider.build(RenamedIdFieldResource.class);
		ResourceField field = resourceInformation.findFieldByName("project");
		Assert.assertNotNull(field);
		Assert.assertNull(resourceInformation.findFieldByName("projectFk"));

		Assert.assertTrue(field.hasIdField());

		RenamedIdFieldResource resource = new RenamedIdFieldResource();
		Assert.assertNull(field.getIdAccessor().getValue(resource));
		field.getIdAccessor().setValue(resource, 13L);
		Assert.assertEquals(13L, resource.getProjectFk().longValue());
		Assert.assertEquals(13L, field.getIdAccessor().getValue(resource));
	}

	@Test
	public void shouldFailForUnmatchedRelationId() {
		try {
			resourceInformationProvider.build(UnmatchedIdFieldResource.class);
			Assert.fail("pro");
		}
		catch (InvalidResourceException e) {
			Assert.assertTrue(e.getMessage().contains("@JsonApiRelationId"));
			Assert.assertTrue(e.getMessage().contains("[projectFk]"));
		}
	}


	@JsonApiResource(type = "renamed")
	public static class RenamedIdFieldResource {

		@JsonApiId
		private Long id;


		@JsonApiRelationId
		private Long projectFk;

		@JsonApiRelation(idField = "projectFk")
		private Project project;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getProjectFk() {
			return projectFk;
		}

		public void setProjectFk(Long projectId) {
			this.projectFk = projectId;
			this.project = null;
		}

		public Project getProject() {
			return project;
		}

		public void setProject(Project project) {
			this.projectFk = project != null ? project.getId() : null;
			this.project = project;
		}
	}


	@JsonApiResource(type = "unmatched")
	public static class UnmatchedIdFieldResource {

		@JsonApiId
		private Long id;


		@JsonApiRelationId
		private Long projectFk;


		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getProjectFk() {
			return projectFk;
		}

		public void setProjectFk(Long projectId) {
			this.projectFk = projectId;
		}
	}
}
