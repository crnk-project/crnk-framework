package io.crnk.core.engine.internal.document.mapper.lookup.relationid;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class MultiValuedRelationIdLookupTest extends AbstractDocumentMapperTest {


    private ScheduleRepositoryImpl scheduleRepository;

    private Schedule schedule3;

    private Schedule schedule4;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Before
    public void setup() {
        super.setup();

        scheduleRepository = (ScheduleRepositoryImpl) (ResourceRepository) container.getRepository(Schedule.class);
        schedule3 = new Schedule();
        schedule3.setId(3L);
        schedule3.setName("test");
        scheduleRepository.save(schedule3);

        schedule4 = new Schedule();
        schedule4.setId(4L);
        schedule4.setName("test");
        scheduleRepository.save(schedule4);
    }


    @Test
    public void checkOnlyIdSet() {
        check(false, true);
    }

    @Test
    public void checkNull() {
        check(false, false);
    }

    @Test
    public void checkEntitySet() {
        check(true, true);
    }

    private void check(boolean setRelatedEntity, boolean setRelatedId) {
        RelationIdTestResource entity = new RelationIdTestResource();
        entity.setId(2L);
        entity.setName("test");
        if (setRelatedId) {
            entity.setTestMultipleValueIds(Arrays.asList(3L, 4L));
        } else {
            entity.setTestMultipleValueIds(null);
        }
        if (setRelatedEntity) {
            entity.setTestMultipleValues(Arrays.asList(schedule3, schedule4));
        }

        QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
        querySpec.includeRelation(Arrays.asList("testMultipleValues"));

        Document document = mapper.toDocument(toResponse(entity), toAdapter(querySpec), mappingConfig).get();
        Resource resource = document.getSingleData().get();
        Assert.assertEquals("2", resource.getId());
        Assert.assertEquals("relationIdTest", resource.getType());
        Assert.assertEquals("test", resource.getAttributes().get("name").asText());

        Nullable<ResourceIdentifier> data = resource.getRelationships().get("testMultipleValues").getSingleData();
        Assert.assertTrue(data.isPresent());

        if (setRelatedId) {
            Assert.assertNotNull(data.get());
            Assert.assertEquals(2, document.getIncluded().size());
            Assert.assertEquals("3", document.getIncluded().get(0).getId());
            Assert.assertEquals("4", document.getIncluded().get(1).getId());
            if (setRelatedEntity) {
                Assert.assertEquals(0, scheduleRepository.getNumFindAll());
            } else {
                Assert.assertEquals(1, scheduleRepository.getNumFindAll());
            }
        } else {
            Assert.assertNull(data.get());
        }
    }

}
