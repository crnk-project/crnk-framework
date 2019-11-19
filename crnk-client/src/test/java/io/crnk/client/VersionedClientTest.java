package io.crnk.client;

import java.io.IOException;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.version.VersionModule;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.VersionedTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VersionedClientTest extends AbstractClientTest {

	private ResourceRepository<VersionedTask, Long> taskRepo;

	private CrnkBoot boot;

	@Before
	public void setup() {
		super.setup();

		client.addModule(new VersionModule());

		taskRepo = client.getRepositoryForType(VersionedTask.class);
	}

	@Test
	public void versionsAvailableFromInformationModel() {
		ResourceInformation resourceInformation = boot.getResourceRegistry().getEntry(VersionedTask.class).getResourceInformation();
		Assert.assertEquals(0, resourceInformation.getVersionRange().getMin());
		Assert.assertEquals(5, resourceInformation.getVersionRange().getMax());

		ResourceField field = resourceInformation.findFieldByUnderlyingName("completed");
		Assert.assertEquals(1, field.getVersionRange().getMin());
		Assert.assertEquals(3, field.getVersionRange().getMax());
		Assert.assertSame(field, resourceInformation.findFieldByJsonName("completed", 1));
		Assert.assertSame(field, resourceInformation.findFieldByJsonName("completed", 2));
		Assert.assertSame(field, resourceInformation.findFieldByJsonName("completed", 3));

		ResourceField newField = resourceInformation.findFieldByUnderlyingName("newCompleted");
		Assert.assertEquals(5, newField.getVersionRange().getMin());
		Assert.assertEquals(Integer.MAX_VALUE, newField.getVersionRange().getMax());
		Assert.assertSame(newField, resourceInformation.findFieldByJsonName("completed", 5));
		Assert.assertSame(newField, resourceInformation.findFieldByJsonName("completed", 6));
		Assert.assertSame(newField, resourceInformation.findFieldByJsonName("completed", 7));
	}

	@Test
	public void noVersionInAcceptHeaderByDefaultToGetLatest() {
		VersionedTask task = new VersionedTask();
		task.setId(1L);
		task.setCompleted(true);
		task.setName("someTask");
		taskRepo.create(task);

		assertHasHeaderValue(HttpHeaders.HTTP_HEADER_ACCEPT, HttpHeaders.JSONAPI_CONTENT_TYPE);
	}

	@Test
	public void versionInAcceptHeaderIfConfigured() {
		client.setVersion(3);

		VersionedTask task = new VersionedTask();
		task.setId(1L);
		task.setCompleted(true);
		task.setName("someTask");
		taskRepo.create(task);

		assertHasHeaderValue(HttpHeaders.HTTP_HEADER_ACCEPT, HttpHeaders.JSONAPI_CONTENT_TYPE + "; version=3");
	}

	@Test
	public void fieldNotRequestedIfOutOfRange() {
		client.setVersion(1);
		VersionedTask task = new VersionedTask();
		task.setId(1L);
		task.setCompleted(true);
		task.setName("someTask");
		task = taskRepo.create(task);
		Assert.assertTrue(task.isCompleted());

		// check within range
		client.setVersion(3);
		task = taskRepo.findOne(task.getId(), new QuerySpec(VersionedTask.class));
		Assert.assertTrue(task.isCompleted());

		// check outside range
		client.setVersion(4); // completed only 1 to 3
		task = taskRepo.findOne(task.getId(), new QuerySpec(VersionedTask.class));
		Assert.assertFalse(task.isCompleted());
	}

	@Test
	public void fieldNotPostedOutOfRange() {
		client.setVersion(4); // leave range
		VersionedTask task = new VersionedTask();
		task.setId(1L);
		task.setCompleted(true);
		task.setName("someTask");
		task = taskRepo.create(task);
		Assert.assertFalse(task.isCompleted());

		client.setVersion(3);
		task = taskRepo.findOne(task.getId(), new QuerySpec(VersionedTask.class));
		Assert.assertFalse(task.isCompleted());
	}

	@Test
	public void postNewVersionOfField() {
		client.setVersion(5); // range of new version
		VersionedTask task = new VersionedTask();
		task.setId(1L);
		task.setCompleted(true);
		task.setNewCompleted(VersionedTask.CompletionStatus.IN_PROGRESS);
		task.setName("someTask");
		task = taskRepo.create(task);
		Assert.assertFalse(task.isCompleted());
		Assert.assertEquals(VersionedTask.CompletionStatus.IN_PROGRESS, task.getNewCompleted());

		task = taskRepo.findOne(task.getId(), new QuerySpec(VersionedTask.class));
		Assert.assertFalse(task.isCompleted());
		Assert.assertEquals(VersionedTask.CompletionStatus.IN_PROGRESS, task.getNewCompleted());

		// move out of new version range
		client.setVersion(3);
		task = taskRepo.findOne(task.getId(), new QuerySpec(VersionedTask.class));
		Assert.assertFalse(task.isCompleted());
		Assert.assertNull(task.getNewCompleted());
	}

	@Test
	public void checkVersionAsParameter() throws IOException {
		String url = client.getServiceUrlProvider().getUrl() + "/versionedTask?version=3";
		HttpAdapter httpAdapter = client.getHttpAdapter();
		HttpAdapterRequest request = httpAdapter.newRequest(url, HttpMethod.GET, null);
		HttpAdapterResponse response = request.execute();
		Assert.assertEquals(200, response.code());
	}


	@Test
	public void checkVersionAsParameterOutOfRange() throws IOException {
		String url = client.getServiceUrlProvider().getUrl() + "/versionedTask?version=6";
		HttpAdapter httpAdapter = client.getHttpAdapter();
		HttpAdapterRequest request = httpAdapter.newRequest(url, HttpMethod.GET, null);
		HttpAdapterResponse response = request.execute();
		Assert.assertEquals(403, response.code());
	}


	@Test(expected = ForbiddenException.class)
	public void resourceNotAvailableOutOfRange() {
		client.setVersion(6); // leave range of entire resource
		VersionedTask task = new VersionedTask();
		task.setId(1L);
		taskRepo.create(task);
	}

	@Test
	public void registryHoldsProperLatestVersion() {
		Assert.assertEquals(5, boot.getResourceRegistry().getLatestVersion());
	}

	protected void setupFeature(CrnkTestFeature feature) {
		feature.addModule(new VersionModule());
		boot = feature.getBoot();
	}
}
