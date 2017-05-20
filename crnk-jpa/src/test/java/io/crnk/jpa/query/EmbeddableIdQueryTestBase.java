package io.crnk.jpa.query;

import io.crnk.jpa.model.TestEmbeddedIdEntity;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

@Transactional
public abstract class EmbeddableIdQueryTestBase extends AbstractJpaTest {

	private JpaQuery<TestEmbeddedIdEntity> builder() {
		return queryFactory.query(TestEmbeddedIdEntity.class);
	}

	@Test
	public void testAll() {
		assertEquals(5, builder().buildExecutor().getResultList().size());
	}

}
