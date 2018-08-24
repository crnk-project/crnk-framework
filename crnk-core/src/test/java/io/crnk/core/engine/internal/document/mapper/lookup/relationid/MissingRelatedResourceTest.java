package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.document.mapper.DocumentMapperTest;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.Test;

import java.util.Arrays;

public class MissingRelatedResourceTest extends DocumentMapperTest {

	@Test(expected = ResourceNotFoundException.class)
	public void provokeResourceNotFound() {
		RelationIdTestResource entity = new RelationIdTestResource();
		entity.setId(0L);
		entity.setTestResourceIdRefId(new ResourceIdentifier("1", "schedules"));

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		querySpec.includeRelation(Arrays.asList("testResourceIdRef"));

		mapper.toDocument(toResponse(entity), toAdapter(querySpec), mappingConfig).get();
	}
}
