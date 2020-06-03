package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class FieldsTest extends MetaResourceBaseTest {

	@Test
	void parameter() {
		Parameter parameter = new Fields(metaResource).parameter();
		Assert.assertEquals("fields", parameter.getName());
		Assert.assertEquals("query", parameter.getIn());
		Assert.assertEquals("ResourceType fields to include (csv)", parameter.getDescription());
		Assert.assertNull(parameter.getRequired());
		Schema schema = parameter.getSchema();
		Assert.assertTrue(schema instanceof StringSchema);
		Assert.assertEquals("id,name,resourceRelation", schema.getDefault());  // TODO: Should Primary Key Field(s) be included?
	}
}
