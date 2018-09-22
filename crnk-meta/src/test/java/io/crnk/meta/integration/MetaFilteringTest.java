package io.crnk.meta.integration;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterBase;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class MetaFilteringTest extends AbstractMetaJerseyTest {


	private ResourceRepositoryV2<MetaResource, Serializable> repository;

	private ResourceFilter filter;


	@Before
	public void setup() {
		super.setup();
		repository = client.getRepositoryForType(MetaResource.class);
	}

	@Override
	protected void setupFeature(CrnkFeature feature) {
		filter = Mockito.spy(ResourceFilterBase.class);
		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addResourceFilter(filter);
		feature.addModule(filterModule);
	}


	@Test
	public void checkNoResourceFiltering() throws IOException {
		Mockito.when(filter.filterResource(Mockito.any(ResourceInformation.class), Mockito.any(HttpMethod.class))).
				thenReturn(FilterBehavior.NONE);
		checkResourceMeta(true, true, true, true);
	}

	@Test
	public void checkTotalResourceFiltering() throws IOException {
		RegistryEntry entry = boot.getResourceRegistry().getEntry(Task.class);
		ResourceInformation resourceInformation = entry.getResourceInformation();

		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
		checkResourceMeta(false, false, false, false);
	}


	@Test
	public void checkReadOnlyResource() throws IOException {
		RegistryEntry entry = boot.getResourceRegistry().getEntry(Task.class);
		ResourceInformation resourceInformation = entry.getResourceInformation();

		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.POST))).thenReturn(FilterBehavior.FORBIDDEN);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.FORBIDDEN);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.DELETE))).thenReturn(FilterBehavior.FORBIDDEN);
		checkResourceMeta(true, false, false, false);
	}

	@Test
	public void checkReadOnlyInsertableResource() throws IOException {
		RegistryEntry entry = boot.getResourceRegistry().getEntry(Task.class);
		ResourceInformation resourceInformation = entry.getResourceInformation();

		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.POST))).thenReturn(FilterBehavior.NONE);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.FORBIDDEN);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.DELETE))).thenReturn(FilterBehavior.FORBIDDEN);
		checkResourceMeta(true, true, false, false);
	}

	@Test
	public void checkReadOnlyUpdatableResource() throws IOException {
		RegistryEntry entry = boot.getResourceRegistry().getEntry(Task.class);
		ResourceInformation resourceInformation = entry.getResourceInformation();

		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.POST))).thenReturn(FilterBehavior.FORBIDDEN);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.NONE);
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.DELETE))).thenReturn(FilterBehavior.FORBIDDEN);
		checkResourceMeta(true, false, true, false);
	}


	@Test
	public void checkFilterRelationship() throws IOException {
		RegistryEntry projectEntry = boot.getResourceRegistry().getEntry(Project.class);
		ResourceInformation projectResourceInformation = projectEntry.getResourceInformation();

		Mockito.when(filter.filterResource(Mockito.eq(projectResourceInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);

		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "Tasks"));
		ResourceList<MetaResource> list = repository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		MetaResource taskMeta = list.get(0);

		// project attribute not available as opposite project type not available
		Assert.assertFalse(taskMeta.hasAttribute("project"));
	}


	@Test
	public void checkReadOnlyAttribute() throws IOException {
		RegistryEntry entry = boot.getResourceRegistry().getEntry(Task.class);
		ResourceInformation resourceInformation = entry.getResourceInformation();

		ResourceField projectField = resourceInformation.findFieldByUnderlyingName("project");
		Mockito.when(filter.filterField(Mockito.eq(projectField), Mockito.eq(HttpMethod.POST))).thenReturn(FilterBehavior.FORBIDDEN);
		Mockito.when(filter.filterField(Mockito.eq(projectField), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.FORBIDDEN);

		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "Tasks"));
		ResourceList<MetaResource> list = repository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		MetaResource taskMeta = list.get(0);

		Assert.assertTrue(taskMeta.hasAttribute("project"));
		MetaAttribute projectAttr = taskMeta.getAttribute("project");
		Assert.assertFalse(projectAttr.isInsertable());
		Assert.assertFalse(projectAttr.isUpdatable());
	}

	@Test
	public void checkInsertableAttribute() throws IOException {
		RegistryEntry entry = boot.getResourceRegistry().getEntry(Task.class);
		ResourceInformation resourceInformation = entry.getResourceInformation();

		ResourceField projectField = resourceInformation.findFieldByUnderlyingName("project");
		Mockito.when(filter.filterField(Mockito.eq(projectField), Mockito.eq(HttpMethod.POST))).thenReturn(FilterBehavior.NONE);
		Mockito.when(filter.filterField(Mockito.eq(projectField), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.FORBIDDEN);

		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "Tasks"));
		ResourceList<MetaResource> list = repository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		MetaResource taskMeta = list.get(0);

		Assert.assertTrue(taskMeta.hasAttribute("project"));
		MetaAttribute projectAttr = taskMeta.getAttribute("project");
		Assert.assertTrue(projectAttr.isInsertable());
		Assert.assertFalse(projectAttr.isUpdatable());
	}


	private void checkResourceMeta(boolean readable, boolean insertable, boolean updatable, boolean deletable) throws IOException {
		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "Tasks"));
		ResourceList<MetaResource> list = repository.findAll(querySpec);
		boolean filtered = !readable && !insertable && !updatable && !deletable;
		Assert.assertEquals(filtered, list.isEmpty());
		if (!filtered) {
			MetaResource metaResource = list.get(0);
			Assert.assertEquals(readable, metaResource.isReadable());
			Assert.assertEquals(deletable, metaResource.isDeletable());
			Assert.assertEquals(insertable, metaResource.isInsertable());
			Assert.assertEquals(updatable, metaResource.isUpdatable());

			MetaAttribute idAttr = metaResource.getAttribute("id");
			Assert.assertNotNull(idAttr);
		}
	}
}
