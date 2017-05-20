package io.crnk.core.resource;

import io.crnk.core.engine.internal.utils.CastableInformation;
import io.crnk.core.mock.models.Task;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.MetaInformation;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;

public class ResourceListTest {

	@Test
	public void defaultConstructor() {
		DefaultResourceList<Task> list = new DefaultResourceList<Task>();
		Assert.assertNull(list.getMeta());
		Assert.assertNull(list.getLinks());
		Assert.assertNull(list.getMeta(TestMeta.class));
		Assert.assertNull(list.getLinks(TestLinks.class));
		Assert.assertNull(list.getMetaInformation(TestMeta.class));
		Assert.assertNull(list.getLinksInformation(TestLinks.class));
		Assert.assertNull(list.getMetaInformation());
		Assert.assertNull(list.getLinksInformation());
	}

	@Test
	public void setters() {
		ResourceListBase<Task, TestMeta, TestLinks> list = new ResourceListBase<Task, TestMeta, TestLinks>() {

		};
		list.setLinks(new TestLinks());
		list.setMeta(new TestMeta());
		Assert.assertNotNull(list.getMeta());
		Assert.assertNotNull(list.getLinks());
	}

	@Test
	public void defaultInformationConstructor() {
		DefaultResourceList<Task> list = new DefaultResourceList<Task>(new TestMeta(), new TestLinks());
		Assert.assertNotNull(list.getMeta());
		Assert.assertNotNull(list.getLinks());
	}

	@Test
	public void testListConstructor() {
		LinkedList<Task> linkedList = new LinkedList<Task>();
		DefaultResourceList<Task> list = new DefaultResourceList<Task>(linkedList, new TestMeta(), new TestLinks());
		Assert.assertNotNull(list.getMeta());
		Assert.assertNotNull(list.getLinks());
		list.add(new Task());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(1, linkedList.size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void casting() {
		DefaultResourceList<Task> list = new DefaultResourceList<Task>(new TestMeta(), new TestLinks());

		TestMeta testMeta = list.getMeta(TestMeta.class);
		Assert.assertNotNull(testMeta);
		testMeta = list.getMetaInformation(TestMeta.class);
		Assert.assertNotNull(testMeta);
		OtherMeta otherMeta = list.getMeta(OtherMeta.class);
		Assert.assertNotNull(otherMeta);

		TestLinks testLinks = list.getLinks(TestLinks.class);
		Assert.assertNotNull(testLinks);
		testLinks = list.getLinksInformation(TestLinks.class);
		Assert.assertNotNull(testLinks);
		OtherLinks otherLinks = list.getLinks(OtherLinks.class);
		Assert.assertNotNull(otherLinks);
	}

	class TestLinks implements LinksInformation, CastableInformation<LinksInformation> {

		public String name = "value";

		@SuppressWarnings("unchecked")
		@Override
		public <L extends LinksInformation> L as(Class<L> linksClass) {
			return (L) new OtherLinks();
		}
	}

	class TestMeta implements MetaInformation, CastableInformation<MetaInformation> {

		public String name = "value";

		@SuppressWarnings("unchecked")
		@Override
		public <L extends MetaInformation> L as(Class<L> linksClass) {
			return (L) new OtherMeta();
		}

	}

	class OtherLinks implements LinksInformation {

		public String name = "value";
	}

	class OtherMeta implements MetaInformation {

		public String name = "value";

	}

}
