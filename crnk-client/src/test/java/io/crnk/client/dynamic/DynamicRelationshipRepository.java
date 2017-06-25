package io.crnk.client.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.UntypedRelationshipRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DynamicRelationshipRepository implements UntypedRelationshipRepository<Resource, String, Resource, String> {

	private static Map<String, Resource> RESOURCES = new HashMap<>();


	public static void clear() {
		RESOURCES.clear();
	}

	@Override
	public String getSourceResourceType() {
		return "dynamic";
	}

	@Override
	public String getTargetResourceType() {
		return "dynamic";
	}

	@Override
	public Class<Resource> getSourceResourceClass() {
		return Resource.class;
	}

	@Override
	public Class<Resource> getTargetResourceClass() {
		return Resource.class;
	}

	@Override
	public void setRelation(Resource source, String targetId, String fieldName) {
		Assert.assertEquals(targetId, "12");
	}

	@Override
	public void setRelations(Resource source, Iterable<String> targetIds, String fieldName) {
		String targetId = targetIds.iterator().next();
		Assert.assertEquals(targetId, "12");
	}

	@Override
	public void addRelations(Resource source, Iterable<String> targetIds, String fieldName) {
		String targetId = targetIds.iterator().next();
		Assert.assertEquals(targetId, "12");
	}

	@Override
	public void removeRelations(Resource source, Iterable<String> targetIds, String fieldName) {
		String targetId = targetIds.iterator().next();
		Assert.assertEquals(targetId, "12");
	}

	@Override
	public Resource findOneTarget(String sourceId, String fieldName, QuerySpec querySpec) {
		return createResource();
	}


	@Override
	public ResourceList<Resource> findManyTargets(String sourceId, String fieldName, QuerySpec querySpec) {
		DefaultResourceList<Resource> list = new DefaultResourceList<>();
		list.add(createResource());
		return list;
	}


	private Resource createResource() {
		ObjectMapper mapper = new ObjectMapper();
		Resource resource = new Resource();
		resource.setId("john");
		resource.setType("tasks");
		try {
			resource.getAttributes().put("value", mapper.readTree("\"doe\""));
		} catch (IOException e) {
			throw new IllegalArgumentException();
		}
		return resource;
	}
}