package io.crnk.core.engine.filter;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.UserRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.mock.repository.UserToTaskRepository;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RepositoryFilterTest {

	private RepositoryFilter filter = Mockito.spy(new RepositoryFilterBase());

	private QuerySpecAdapter queryAdapter;

	private ResourceRepositoryAdapter resourceAdapter;

	private QuerySpec querySpec;

	private RelationshipRepositoryAdapter projectRelationAdapter;

	private User user1;

	private User user2;

	private RelationshipRepositoryAdapter taskRelationAdapter;

	private ResourceField assignedTasksField;

	private ResourceField assignedProjectsField;

	private ResourceInformation userInfo;

	private ResourceInformation scheduleInfo;

	private CoreTestContainer container;

	@Before
	@After
	public void cleanup() {
		UserRepository.clear();
		UserToProjectRepository.clear();
	}

	@Before
	public void prepare() {
		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addRepositoryFilter(filter);

		container = new CoreTestContainer();
		container.setDefaultPackage();
		container.addModule(filterModule);
		container.boot();

		querySpec = new QuerySpec(User.class);
		querySpec.setPagingSpec(new OffsetLimitPagingSpec());
		queryAdapter = container.toQueryAdapter(querySpec);

		scheduleInfo = container.getEntry(Schedule.class).getResourceInformation();
		RegistryEntry userEntry = container.getEntry(User.class);
		resourceAdapter = userEntry.getResourceRepository();
		projectRelationAdapter = userEntry.getRelationshipRepository("assignedProjects");
		taskRelationAdapter = userEntry.getRelationshipRepository("assignedTasks");
		userInfo = userEntry.getResourceInformation();

		UserRepository resourceRepository = (UserRepository) resourceAdapter.getResourceRepository();
		user1 = new User();
		user1.setId(1L);
		resourceRepository.save(user1);
		user2 = new User();
		user2.setId(2L);
		resourceRepository.save(user2);

		UserToProjectRepository userProjectRepository =
				(UserToProjectRepository) projectRelationAdapter.getRelationshipRepository();
		userProjectRepository.setRelation(user1, 11L, "assignedProjects");

		UserToTaskRepository userTaskRepository = new UserToTaskRepository();
		userTaskRepository.addRelations(user1, Arrays.asList(21L), "assignedTasks");
		userTaskRepository.addRelations(user2, Arrays.asList(22L), "assignedTasks");

		assignedTasksField = userEntry.getResourceInformation().findRelationshipFieldByName("assignedTasks");
		assignedProjectsField = userEntry.getResourceInformation().findRelationshipFieldByName("assignedProjects");
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void findAllWithResourceListResult() throws Exception {

		RegistryEntry scheduleRegistry = container.getEntry(Schedule.class);
		ResourceRepositoryAdapter scheduleResourceAdapter = scheduleRegistry.getResourceRepository();

		querySpec = new QuerySpec(Schedule.class);
		querySpec.setPagingSpec(new OffsetLimitPagingSpec());
		queryAdapter = container.toQueryAdapter(querySpec);
		scheduleResourceAdapter.findAll(queryAdapter);

		ArgumentCaptor<Iterable> linksResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<Iterable> metaResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterLinks(Mockito.any(RepositoryFilterContext.class), linksResources
						.capture(),
				Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterMeta(Mockito.any(RepositoryFilterContext.class), metaResources.capture(),
				Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, linksResources.getAllValues().size());
		Assert.assertEquals(1, metaResources.getAllValues().size());
		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertNull(requestSpec.getId());
		Assert.assertNull(requestSpec.getIds());
		QuerySpec actualQuerySpec = requestSpec.getQuerySpec(scheduleInfo);
		Assert.assertSame(querySpec, actualQuerySpec);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void findAllWithResourceList() throws Exception {
		resourceAdapter.findAll(queryAdapter);

		ArgumentCaptor<Iterable> linksResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<Iterable> metaResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterLinks(Mockito.any(RepositoryFilterContext.class), linksResources
						.capture(),
				Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterMeta(Mockito.any(RepositoryFilterContext.class), metaResources.capture(),
				Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, linksResources.getAllValues().size());
		Assert.assertEquals(1, metaResources.getAllValues().size());
		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertNull(requestSpec.getId());
		Assert.assertNull(requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void findOne() throws Exception {

		resourceAdapter.findOne(1L, queryAdapter);

		ArgumentCaptor<Iterable> linksResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<Iterable> metaResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterLinks(Mockito.any(RepositoryFilterContext.class), linksResources
						.capture(),
				Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterMeta(Mockito.any(RepositoryFilterContext.class), metaResources.capture(),
				Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, linksResources.getAllValues().size());
		Assert.assertEquals(1, metaResources.getAllValues().size());
		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(1L, requestSpec.getId());
		Assert.assertEquals(Collections.singleton(1L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void findAllById() throws Exception {
		resourceAdapter.findAll(Arrays.asList(2L), queryAdapter);

		ArgumentCaptor<Iterable> linksResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<Iterable> metaResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterLinks(Mockito.any(RepositoryFilterContext.class), linksResources
						.capture(),
				Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterMeta(Mockito.any(RepositoryFilterContext.class), metaResources.capture(),
				Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, linksResources.getAllValues().size());
		Assert.assertEquals(1, metaResources.getAllValues().size());
		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(2L, requestSpec.getId());
		Assert.assertEquals(HttpMethod.GET, requestSpec.getMethod());
		Assert.assertEquals(Arrays.asList(2L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void create() throws Exception {
		User user = new User();
		user.setId(3L);
		resourceAdapter.create(user, queryAdapter);

		ArgumentCaptor<Iterable> linksResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<Iterable> metaResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterLinks(Mockito.any(RepositoryFilterContext.class), linksResources
						.capture(),
				Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterMeta(Mockito.any(RepositoryFilterContext.class), metaResources.capture(),
				Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, linksResources.getAllValues().size());
		Assert.assertEquals(1, metaResources.getAllValues().size());
		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(3L, requestSpec.getId());
		Assert.assertEquals(HttpMethod.POST, requestSpec.getMethod());
		Assert.assertEquals(Collections.singleton(3L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void save() throws Exception {
		User user = new User();
		user.setId(3L);
		resourceAdapter.update(user, queryAdapter);

		ArgumentCaptor<Iterable> linksResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<Iterable> metaResources = ArgumentCaptor.forClass(Iterable.class);
		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterLinks(Mockito.any(RepositoryFilterContext.class), linksResources
						.capture(),
				Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1)).filterMeta(Mockito.any(RepositoryFilterContext.class), metaResources.capture(),
				Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, linksResources.getAllValues().size());
		Assert.assertEquals(1, metaResources.getAllValues().size());
		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(3L, requestSpec.getId());
		Assert.assertEquals(HttpMethod.PATCH, requestSpec.getMethod());
		Assert.assertEquals(Collections.singleton(3L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void delete() throws Exception {
		resourceAdapter.delete(2L, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(2L, requestSpec.getId());
		Assert.assertEquals(HttpMethod.DELETE, requestSpec.getMethod());
		Assert.assertEquals(Arrays.asList(2L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void findOneTarget() throws Exception {
		projectRelationAdapter.findOneTarget(1L, assignedProjectsField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(1L, requestSpec.getId());
		Assert.assertEquals(HttpMethod.GET, requestSpec.getMethod());
		Assert.assertEquals(Arrays.asList(1L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void findManyTarget() throws Exception {
		projectRelationAdapter.findManyTargets(1L, assignedProjectsField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(1L, requestSpec.getId());
		Assert.assertEquals("assignedProjects", requestSpec.getRelationshipField().getUnderlyingName());
		Assert.assertEquals(HttpMethod.GET, requestSpec.getMethod());
		Assert.assertEquals(Arrays.asList(1L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void setRelation() throws Exception {
		projectRelationAdapter.setRelation(user1, 13L, assignedProjectsField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(13L, requestSpec.getId());
		Assert.assertEquals(user1, requestSpec.getEntity());
		Assert.assertEquals("assignedProjects", requestSpec.getRelationshipField().getUnderlyingName());
		Assert.assertEquals(HttpMethod.PATCH, requestSpec.getMethod());
		Assert.assertEquals(Arrays.asList(13L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void setRelations() throws Exception {
		projectRelationAdapter.setRelations(user1, Arrays.asList(13L, 14L), assignedProjectsField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(user1, requestSpec.getEntity());
		Assert.assertEquals("assignedProjects", requestSpec.getRelationshipField().getUnderlyingName());
		Assert.assertEquals(HttpMethod.PATCH, requestSpec.getMethod());
		Assert.assertEquals(Arrays.asList(13L, 14L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void addRelations() throws Exception {
		projectRelationAdapter.addRelations(user1, Arrays.asList(13L, 14L), assignedProjectsField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(user1, requestSpec.getEntity());
		Assert.assertEquals(HttpMethod.POST, requestSpec.getMethod());
		Assert.assertEquals(Arrays.asList(13L, 14L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void removeRelations() throws Exception {
		projectRelationAdapter.removeRelations(user1, Arrays.asList(13L, 14L), assignedProjectsField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(1))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context = contexts.getAllValues().iterator().next();
		RepositoryRequestSpec requestSpec = context.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec.getQueryAdapter());
		Assert.assertEquals(user1, requestSpec.getEntity());
		Assert.assertEquals("assignedProjects", requestSpec.getRelationshipField().getUnderlyingName());
		Assert.assertEquals(HttpMethod.DELETE, requestSpec.getMethod());
		Assert.assertEquals(Arrays.asList(13L, 14L), requestSpec.getIds());
		Assert.assertSame(querySpec, requestSpec.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void findBulkOneTargetsNoBulkImpl() throws Exception {
		projectRelationAdapter.findBulkOneTargets(Arrays.asList(13L, 14L), assignedProjectsField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(2))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterBulkRequest(contexts.capture(), Mockito.any(RepositoryBulkRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(2, contexts.getAllValues().size());
		RepositoryFilterContext context1 = contexts.getAllValues().get(0);
		RepositoryFilterContext context2 = contexts.getAllValues().get(1);
		RepositoryRequestSpec requestSpec1 = context1.getRequest();
		RepositoryRequestSpec requestSpec2 = context2.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec1.getQueryAdapter());
		Assert.assertNull(requestSpec1.getEntity());
		Assert.assertEquals("assignedProjects", requestSpec1.getRelationshipField().getUnderlyingName());
		Assert.assertEquals(HttpMethod.GET, requestSpec1.getMethod());
		Assert.assertEquals(13L, requestSpec1.getId());
		Assert.assertEquals(14L, requestSpec2.getId());
		Assert.assertSame(querySpec, requestSpec1.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void findBulkManyTargetsNoBulkImpl() throws Exception {
		projectRelationAdapter.findBulkManyTargets(Arrays.asList(13L, 14L), assignedProjectsField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(2))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(0))
				.filterBulkRequest(contexts.capture(), Mockito.any(RepositoryBulkRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(2, contexts.getAllValues().size());
		RepositoryFilterContext context1 = contexts.getAllValues().get(0);
		RepositoryFilterContext context2 = contexts.getAllValues().get(1);
		RepositoryRequestSpec requestSpec1 = context1.getRequest();
		RepositoryRequestSpec requestSpec2 = context2.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec1.getQueryAdapter());
		Assert.assertNull(requestSpec1.getEntity());
		Assert.assertEquals("assignedProjects", requestSpec1.getRelationshipField().getUnderlyingName());
		Assert.assertEquals(HttpMethod.GET, requestSpec1.getMethod());
		Assert.assertEquals(13L, requestSpec1.getId());
		Assert.assertEquals(14L, requestSpec2.getId());
		Assert.assertSame(querySpec, requestSpec1.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void findBulkOneTargetsBulkImpl() throws Exception {
		taskRelationAdapter.findBulkManyTargets(Arrays.asList(1L), assignedTasksField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(0))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterBulkRequest(contexts.capture(), Mockito.any(RepositoryBulkRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context1 = contexts.getAllValues().get(0);
		RepositoryRequestSpec requestSpec1 = context1.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec1.getQueryAdapter());
		Assert.assertNull(requestSpec1.getEntity());
		Assert.assertEquals("assignedTasks", requestSpec1.getRelationshipField().getUnderlyingName());
		Assert.assertEquals(HttpMethod.GET, requestSpec1.getMethod());
		Assert.assertEquals(Arrays.asList(1L), requestSpec1.getIds());
		Assert.assertSame(querySpec, requestSpec1.getQuerySpec(userInfo));
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void findBulkManyTargetsBulkImpl() throws Exception {
		List<Long> ids = Arrays.asList(1L, 2L);
		taskRelationAdapter.findBulkManyTargets(ids, assignedTasksField, queryAdapter);

		ArgumentCaptor<RepositoryFilterContext> contexts = ArgumentCaptor.forClass(RepositoryFilterContext.class);

		Mockito.verify(filter, Mockito.times(0))
				.filterRequest(contexts.capture(), Mockito.any(RepositoryRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(1))
				.filterBulkRequest(contexts.capture(), Mockito.any(RepositoryBulkRequestFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterResult(Mockito.any(RepositoryFilterContext.class), Mockito.any(RepositoryResultFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterLinks(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryLinksFilterChain.class));
		Mockito.verify(filter, Mockito.times(2))
				.filterMeta(Mockito.any(RepositoryFilterContext.class), Mockito.any(Iterable.class),
						Mockito.any(RepositoryMetaFilterChain.class));

		Assert.assertEquals(1, contexts.getAllValues().size());
		RepositoryFilterContext context1 = contexts.getAllValues().get(0);
		RepositoryRequestSpec requestSpec1 = context1.getRequest();
		Assert.assertEquals(queryAdapter, requestSpec1.getQueryAdapter());
		Assert.assertNull(requestSpec1.getEntity());
		Assert.assertEquals("assignedTasks", requestSpec1.getRelationshipField().getUnderlyingName());
		Assert.assertEquals(HttpMethod.GET, requestSpec1.getMethod());
		Assert.assertEquals(Arrays.asList(1L, 2L), requestSpec1.getIds());
		Assert.assertSame(querySpec, requestSpec1.getQuerySpec(userInfo));
	}
}
