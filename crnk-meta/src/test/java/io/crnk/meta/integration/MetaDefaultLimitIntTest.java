package io.crnk.meta.integration;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.rs.CrnkFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class MetaDefaultLimitIntTest extends AbstractMetaJerseyTest {

	private ResourceRepositoryV2<MetaResource, Serializable> repository;

	@Before
	public void setup() {
		super.setup();
		repository = client.getRepositoryForType(MetaResource.class);
	}

	@Override
	protected void setupFeature(CrnkFeature feature) {
		feature.setDefaultPageLimit(2L);
	}

	@Test
	public void limitShouldApplyToResults() {
		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.includeRelation(Arrays.asList("attributes"));

		ResourceList<MetaResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());
	}

	@Test
	public void limitShouldNotAffectRelationships() {
		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.includeRelation(Arrays.asList("attributes"));
		querySpec.addFilter(new FilterSpec(Arrays.asList("resourceType"), FilterOperator.EQ, "tasks"));

		ResourceList<MetaResource> list = repository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		MetaResource taskMeta = list.get(0);

		List<? extends MetaAttribute> attributes = taskMeta.getAttributes();
		Assert.assertTrue(attributes.size() > 5);
	}

	@Test
	public void limitShouldNotAffectRelationshipsWithSpecOnRelationship() {
		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.includeRelation(Arrays.asList("attributes"));
		querySpec.addFilter(new FilterSpec(Arrays.asList("resourceType"), FilterOperator.EQ, "tasks"));

		querySpec.getOrCreateQuerySpec(MetaAttribute.class).addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));
		querySpec.getOrCreateQuerySpec(MetaElement.class).addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));
		querySpec.getOrCreateQuerySpec(MetaResourceField.class).addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));

		ResourceList<MetaResource> list = repository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		MetaResource taskMeta = list.get(0);

		List<? extends MetaAttribute> attributes = taskMeta.getAttributes();
		Assert.assertTrue(attributes.size() > 5);
	}

	@Test
	public void limitShouldNotAffectRelationshipsWithMetaElement() {
		ResourceRepositoryV2<MetaElement, Serializable> elementRepository = client.getRepositoryForType(MetaElement.class);

		QuerySpec querySpec = new QuerySpec(MetaElement.class);
		querySpec.includeRelation(Arrays.asList("attributes"));
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, "resources.tasks"));

		ResourceList<MetaElement> list = elementRepository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		MetaResource taskMeta = (MetaResource) list.get(0);

		List<? extends MetaAttribute> attributes = taskMeta.getAttributes();
		Assert.assertTrue(attributes.size() > 5);
	}

	@Test
	public void limitShouldNotAffectRelationshipsWithCustomLimit() {
		QuerySpec querySpec = new QuerySpec(MetaResource.class);
		querySpec.includeRelation(Arrays.asList("attributes"));
		querySpec.setLimit(3L);
		querySpec.addFilter(new FilterSpec(Arrays.asList("resourceType"), FilterOperator.EQ, "tasks"));

		ResourceList<MetaResource> list = repository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		MetaResource taskMeta = list.get(0);

		List<? extends MetaAttribute> attributes = taskMeta.getAttributes();
		Assert.assertTrue(attributes.size() > 5);
	}

}
