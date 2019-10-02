package io.crnk.client;

import io.crnk.client.internal.ResourceRepositoryStubImpl;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.Wrapper;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.repository.ScheduleRepository;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleList;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleListLinks;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleListMeta;
import org.junit.Assert;
import org.junit.Test;

public class RepositoryInterfaceClientTest extends AbstractClientTest {


	@Test
	public void testInterfaceAccess() {
		ScheduleRepository scheduleRepository = client.getRepositoryForInterface(ScheduleRepository.class);

		Schedule schedule = new Schedule();
		schedule.setId(13L);
		schedule.setName("mySchedule");
		scheduleRepository.create(schedule);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		ScheduleList list = scheduleRepository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		ScheduleListMeta meta = list.getMeta();
		ScheduleListLinks links = list.getLinks();
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);
	}

	@Test
	public void testUnwrapRepository() {
		ScheduleRepository scheduleRepository = client.getRepositoryForInterface(ScheduleRepository.class);
		Assert.assertTrue(scheduleRepository instanceof Wrapper);
		Wrapper wrapper = (Wrapper) scheduleRepository;
		Object wrappedObject = wrapper.getWrappedObject();
		Assert.assertEquals(wrappedObject.getClass(), ResourceRepositoryStubImpl.class);
	}

	@Test
	public void testNotExposed() {
		ModuleRegistry moduleRegistry = testApplication.getFeature().getBoot().getModuleRegistry();
		RepositoryInformationProvider provider = moduleRegistry.getRepositoryInformationBuilder();

		ResourceRepository<Schedule, Object> repository = client.getRepositoryForType(Schedule.class);
		ResourceRepositoryInformation information = (ResourceRepositoryInformation) provider.build(repository, new RepositoryInformationProviderContext() {
			@Override
			public ResourceInformationProvider getResourceInformationBuilder() {
				return moduleRegistry.getResourceInformationBuilder();
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}

			@Override
			public InformationBuilder builder() {
				return moduleRegistry.getInformationBuilder();
			}
		});
		Assert.assertFalse(information.isExposed());
	}
}
