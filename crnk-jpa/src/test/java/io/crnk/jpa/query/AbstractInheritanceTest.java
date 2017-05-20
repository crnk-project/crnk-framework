package io.crnk.jpa.query;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.jpa.meta.MetaEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Transactional
public abstract class AbstractInheritanceTest<B, C> extends AbstractJpaTest {

	private Class<B> baseClass;

	private Class<C> childClass;

	protected AbstractInheritanceTest(Class<B> baseClass, Class<C> childClass) {
		this.baseClass = baseClass;
		this.childClass = childClass;
	}

	private JpaQuery<B> baseBuilder() {
		return queryFactory.query(baseClass);
	}

	@Test
	public void testMeta() {
		MetaEntity baseMeta = module.getJpaMetaLookup().getMeta(baseClass, MetaEntity.class);
		MetaEntity childMeta = module.getJpaMetaLookup().getMeta(childClass, MetaEntity.class);
		Assert.assertSame(baseMeta, childMeta.getSuperType());

		Assert.assertEquals(1, childMeta.getDeclaredAttributes().size());
		Assert.assertEquals(2, baseMeta.getAttributes().size());
		Assert.assertEquals(3, childMeta.getAttributes().size());

		Assert.assertNotNull(baseMeta.getAttribute("id"));
		Assert.assertNotNull(baseMeta.getAttribute("stringValue"));
		try {
			Assert.assertNull(baseMeta.getAttribute("intValue"));
			Assert.fail();
		} catch (Exception e) {
			// ok
		}
		Assert.assertNotNull(childMeta.getAttribute("id"));
		Assert.assertNotNull(childMeta.getAttribute("stringValue"));
		Assert.assertNotNull(childMeta.getAttribute("intValue"));
	}

	@Test
	public void testAll() {
		assertEquals(10, baseBuilder().buildExecutor().getResultList().size());
	}

	@Test
	public void testFilterBySubtypeAttribute() {
		// FIXME subtype lookup
		Assert.assertTrue(module.getJpaMetaLookup().getMeta(childClass, MetaEntity.class) instanceof MetaEntity);

		assertEquals(1, baseBuilder().addFilter("intValue", FilterOperator.EQ, 2).buildExecutor().getResultList().size());
		assertEquals(3, baseBuilder().addFilter("intValue", FilterOperator.GT, 1).buildExecutor().getResultList().size());
	}

	@Test
	public void testOrderBySubtypeAttribute() {
		// FIXME subtype lookup
		Assert.assertTrue(module.getJpaMetaLookup().getMeta(childClass, MetaEntity.class) instanceof MetaEntity);

		List<B> list = baseBuilder().addSortBy(Arrays.asList("intValue"), Direction.DESC).buildExecutor().getResultList();
		Assert.assertEquals(10, list.size());
		for (int i = 0; i < 10; i++) {
			B entity = list.get(i);
			MetaEntity meta = (MetaEntity) module.getJpaMetaLookup().getMeta(entity.getClass());

			if (i < 5) {
				Assert.assertTrue(childClass.isInstance(entity));
				Assert.assertEquals(4 - i, meta.getAttribute("intValue").getValue(entity));
			} else {
				Assert.assertFalse(childClass.isInstance(entity));

				// order by primary key by default second order criteria
				Assert.assertEquals(Long.valueOf(i - 5), meta.getAttribute("id").getValue(entity));
			}
		}
	}

}
