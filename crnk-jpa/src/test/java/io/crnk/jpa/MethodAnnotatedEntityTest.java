package io.crnk.jpa;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.jpa.meta.MetaEntity;
import io.crnk.jpa.model.MethodAnnotatedEntity;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MethodAnnotatedEntityTest extends AbstractJpaJerseyTest {

	@Test
	public void testMeta() {
		MethodAnnotatedEntity entity = new MethodAnnotatedEntity();
		entity.setId(13L);
		entity.setStringValue("test");

		MetaEntity meta = jpaMetaProvider.getMeta(MethodAnnotatedEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());

		MetaAttribute stringValueAttr = meta.getAttribute("stringValue");
		Assert.assertNotNull(stringValueAttr);
		Assert.assertEquals("stringValue", stringValueAttr.getName());
		Assert.assertEquals("test", stringValueAttr.getValue(entity));

		MetaAttribute idAttr = meta.getAttribute("id");
		Assert.assertNotNull(idAttr);
		Assert.assertEquals("id", idAttr.getName());
		Assert.assertEquals(13L, idAttr.getValue(entity));

	}

	@Test
	public void testMethodAnnotatedFields() {
		// tests whether JPA annotations on methods are supported as well
		ResourceRepository<MethodAnnotatedEntity, Long> methodRepo = client.getRepositoryForType(MethodAnnotatedEntity.class);

		MethodAnnotatedEntity task = new MethodAnnotatedEntity();
		task.setId(1L);
		task.setStringValue("test");
		methodRepo.create(task);

		// check retrievable with findAll
		List<MethodAnnotatedEntity> list = methodRepo.findAll(new QuerySpec(MethodAnnotatedEntity.class));
		Assert.assertEquals(1, list.size());
		MethodAnnotatedEntity savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findAll(ids)
		list = methodRepo.findAll(Arrays.asList(1L), new QuerySpec(MethodAnnotatedEntity.class));
		Assert.assertEquals(1, list.size());
		savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findOne
		savedTask = methodRepo.findOne(1L, new QuerySpec(MethodAnnotatedEntity.class));
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());
	}
}
