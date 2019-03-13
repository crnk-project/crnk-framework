package io.crnk.client.inheritance.repositories;

import java.util.Collections;
import java.util.List;

import io.crnk.client.inheritance.resources.related.RelatedResourceA;
import io.crnk.client.inheritance.resources.related.RelatedResourceAsub1;
import io.crnk.client.inheritance.resources.related.RelatedResourceB;

/*
 * @author syri.
 */
public class RepositoryData {

	public static final List<RelatedResourceA> RESOURCE_A_LIST;

	public static final List<RelatedResourceB> RESOURCE_B_LIST;

	static {
		RelatedResourceAsub1 relatedResourceAsub1 = new RelatedResourceAsub1();
		relatedResourceAsub1.setId(1L);

		RelatedResourceB relatedResourceB = new RelatedResourceB();
		relatedResourceB.setId(10L);

		relatedResourceAsub1.setRelatedResourceBS(Collections.singletonList(relatedResourceB));
		relatedResourceB.setRelatedResourceAsub1(relatedResourceAsub1);

		RESOURCE_A_LIST = Collections.singletonList(relatedResourceAsub1);
		RESOURCE_B_LIST = Collections.singletonList(relatedResourceB);
	}
}
