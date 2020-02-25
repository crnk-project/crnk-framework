package io.crnk.core.mock.repository;

import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.LinksInformation;

public class TaskLinksInformation implements LinksInformation {
	public Link name = new DefaultLink("value");
}
