package io.crnk.reactive;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.reactive.internal.MonoResultFactory;
import io.crnk.reactive.internal.adapter.WorkerRelationshipRepositoryAdapter;
import io.crnk.reactive.internal.adapter.WorkerResourceRepositoryAdapter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;

public class WorkerRepositoryAdapterTest {

	private RelationshipRepositoryAdapter relAdapter;

	private WorkerRelationshipRepositoryAdapter workerRelAdapter;

	private ResourceRepositoryAdapter adapter;

	private WorkerResourceRepositoryAdapter workerAdapter;

	private Object source = new Object();

	private Object id = 12;
	private List ids = Arrays.asList(12);

	private ResourceField field = Mockito.mock(ResourceField.class);

	private QueryAdapter queryAdapter = Mockito.mock(QueryAdapter.class);

	private Result response;


	@Before
	public void setup() {
		Scheduler scheduler = Schedulers.immediate();
		HttpRequestContextProvider requestContextProvider = Mockito.mock(HttpRequestContextProvider.class);
		relAdapter = Mockito.mock(RelationshipRepositoryAdapter.class);
		workerRelAdapter = new WorkerRelationshipRepositoryAdapter(relAdapter, scheduler, requestContextProvider);

		adapter = Mockito.mock(ResourceRepositoryAdapter.class);
		workerAdapter = new WorkerResourceRepositoryAdapter(adapter, scheduler, requestContextProvider);

		MonoResultFactory resultFactory = new MonoResultFactory();
		response = resultFactory.just(new JsonApiResponse());
		HttpRequestContext requestContext = Mockito.mock(HttpRequestContext.class);
		Result<HttpRequestContext> requestContextResult = resultFactory.just(requestContext);
		Mockito.when(requestContextProvider.getRequestContextResult()).thenReturn(requestContextResult);
	}


	@Test
	public void findOne() {
		Mockito.when(adapter.findOne(Mockito.eq(id), Mockito.eq(queryAdapter))).thenReturn(response);
		workerAdapter.findOne(id, queryAdapter).get();
		Mockito.verify(adapter, Mockito.times(1)).findOne(Mockito.eq(id), Mockito.eq(queryAdapter));
	}

	@Test
	public void findAll() {
		Mockito.when(adapter.findAll(Mockito.eq(queryAdapter))).thenReturn(response);
		workerAdapter.findAll(queryAdapter).get();
		Mockito.verify(adapter, Mockito.times(1)).findAll(Mockito.eq(queryAdapter));
	}

	@Test
	public void findAllIds() {
		Mockito.when(adapter.findAll(Mockito.eq(ids), Mockito.eq(queryAdapter))).thenReturn(response);
		workerAdapter.findAll(ids, queryAdapter).get();
		Mockito.verify(adapter, Mockito.times(1)).findAll(Mockito.eq(ids), Mockito.eq(queryAdapter));
	}


	@Test
	public void update() {
		Mockito.when(adapter.update(Mockito.eq(source), Mockito.eq(queryAdapter))).thenReturn(response);
		workerAdapter.update(source, queryAdapter).get();
		Mockito.verify(adapter, Mockito.times(1)).update(Mockito.eq(source), Mockito.eq(queryAdapter));
	}


	@Test
	public void create() {
		Mockito.when(adapter.create(Mockito.eq(source), Mockito.eq(queryAdapter))).thenReturn(response);
		workerAdapter.create(source, queryAdapter).get();
		Mockito.verify(adapter, Mockito.times(1)).create(Mockito.eq(source), Mockito.eq(queryAdapter));
	}


	@Test
	public void delete() {
		Mockito.when(adapter.delete(Mockito.eq(source), Mockito.eq(queryAdapter))).thenReturn(response);
		workerAdapter.delete(source, queryAdapter).get();
		Mockito.verify(adapter, Mockito.times(1)).delete(Mockito.eq(source), Mockito.eq(queryAdapter));
	}


	@Test
	public void getResourceRepository() {
		workerAdapter.getImplementation();
		Mockito.verify(adapter, Mockito.times(1)).getImplementation();
	}

	@Test
	public void getRepositoryInformation() {
		workerAdapter.getRepositoryInformation();
		Mockito.verify(adapter, Mockito.times(1)).getRepositoryInformation();
	}


	@Test
	public void getRelationshipRepository() {
		workerRelAdapter.getImplementation();
		Mockito.verify(relAdapter, Mockito.times(1)).getImplementation();
	}

	@Test
	public void getResourceField() {
		workerRelAdapter.getResourceField();
		Mockito.verify(relAdapter, Mockito.times(1)).getResourceField();
	}


	@Test
	public void testSetRelation() {
		Mockito.when(relAdapter.setRelation(Mockito.eq(source), Mockito.eq(id), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.setRelation(source, id, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).setRelation(Mockito.eq(source), Mockito.eq(id), Mockito.eq(field), Mockito.eq(queryAdapter));
	}

	@Test
	public void testSetRelations() {
		Mockito.when(relAdapter.setRelations(Mockito.eq(source), Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.setRelations(source, ids, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).setRelations(Mockito.eq(source), Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter));
	}

	@Test
	public void addRelations() {
		Mockito.when(relAdapter.addRelations(Mockito.eq(source), Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.addRelations(source, ids, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).addRelations(Mockito.eq(source), Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter));
	}

	@Test
	public void removeRelations() {
		Mockito.when(relAdapter.removeRelations(Mockito.eq(source), Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.removeRelations(source, ids, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).removeRelations(Mockito.eq(source), Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter));
	}

	@Test
	public void findOneTarget() {
		Mockito.when(relAdapter.findOneRelations(Mockito.eq(id), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.findOneRelations(id, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).findOneRelations(Mockito.eq(id), Mockito.eq(field), Mockito.eq(queryAdapter));
	}


	@Test
	public void findManyTargets() {
		Mockito.when(relAdapter.findManyRelations(Mockito.eq(id), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.findManyRelations(id, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).findManyRelations(Mockito.eq(id), Mockito.eq(field), Mockito.eq(queryAdapter));
	}

	@Test
	public void findIdsManyTargets() {
		Mockito.when(relAdapter.findManyRelations(Mockito.eq(id), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.findManyRelations(id, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).findManyRelations(Mockito.eq(id), Mockito.eq(field), Mockito.eq(queryAdapter));
	}

	@Test
	public void findBulkOneTargets() {
		Mockito.when(relAdapter.findBulkOneTargets(Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.findBulkOneTargets(ids, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).findBulkOneTargets(Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter));
	}

	@Test
	public void findBulkManyTargets() {
		Mockito.when(relAdapter.findBulkManyTargets(Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter))).thenReturn(response);
		workerRelAdapter.findBulkManyTargets(ids, field, queryAdapter).get();
		Mockito.verify(relAdapter, Mockito.times(1)).findBulkManyTargets(Mockito.eq(ids), Mockito.eq(field), Mockito.eq(queryAdapter));
	}
}
