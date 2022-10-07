package io.crnk.core.mock.models;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.util.ArrayList;
import java.util.List;

@JsonApiResource(type = "relationIdTest")
public class RelationIdTestResource {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiRelationId
	private Long testRenamedDifferent;

	@JsonApiRelation(idField = "testRenamedDifferent")
	private Schedule testRenamed;

	@JsonApiRelationId
	private Long testLookupAlwaysId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	private Schedule testLookupAlways;

	@JsonApiRelationId
	private Long testLookupWhenNullId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Schedule testLookupWhenNull;

	@JsonApiRelationId
	private List<Long> testMultipleValueIds = new ArrayList<>();

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private List<Schedule> testMultipleValues = new ArrayList<>();

	@JsonApiRelationId
	private Long testLookupNoneId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.NONE)
	private Schedule testLookupNone;

	@JsonApiRelationId
	private Long testSerializeEagerId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.EAGER)
	private Schedule testSerializeEager;


	@JsonApiRelationId
	private Long testSerializeOnlyIdId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.ONLY_ID)
	private Schedule testSerializeOnlyId;


	@JsonApiRelationId
	private Long testNestedId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private RelationIdTestResource testNested;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "testNested",
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
	private RelationIdTestResource testNestedOpposite;

	@JsonApiRelationId
	private ResourceIdentifier testResourceIdRefId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Schedule testResourceIdRef;

	@JsonApiRelationId
	private Long testSubTypedResourceId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private TopTask testSubTypedResource;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.NONE)
	private List<TopTaskWrapper> topTaskWrappers = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public Long getTestRenamedDifferent() {
		return testRenamedDifferent;
	}

	public void setTestRenamedDifferent(Long testRenamedDifferent) {
		this.testRenamedDifferent = testRenamedDifferent;
	}

	public Schedule getTestRenamed() {
		return testRenamed;
	}

	public void setTestRenamed(Schedule testRenamed) {
		this.testRenamed = testRenamed;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Long> getTestMultipleValueIds() {
		return testMultipleValueIds;
	}

	public void setTestMultipleValueIds(List<Long> testMultipleValueIds) {
		this.testMultipleValueIds = testMultipleValueIds;
		this.testMultipleValues = null;
	}

	public List<Schedule> getTestMultipleValues() {
		return testMultipleValues;
	}

	public void setTestMultipleValues(List<Schedule> testMultipleValues) {
		this.testMultipleValues = testMultipleValues;
		if (testMultipleValues != null) {
			List<Long> ids = new ArrayList<>();
			for (Schedule schedule : testMultipleValues) {
				ids.add(schedule.getId());
			}
			this.testMultipleValueIds = ids;
		} else {
			this.testMultipleValueIds = null;
		}
	}

	public Long getTestNestedId() {
		return testNestedId;
	}

	public void setTestNestedId(Long testNestedId) {
		this.testNestedId = testNestedId;
		this.testNested = null;
	}

	public RelationIdTestResource getTestNested() {
		return testNested;
	}

	public void setTestNested(RelationIdTestResource testNested) {
		this.testNested = testNested;
		this.testNestedId = testNested != null ? testNested.getId() : null;
	}

	public RelationIdTestResource getTestNestedOpposite() {
		return testNestedOpposite;
	}

	public void setTestNestedOpposite(RelationIdTestResource testNestedOpposite) {
		this.testNestedOpposite = testNestedOpposite;
	}

	public Long getTestLookupAlwaysId() {
		return testLookupAlwaysId;
	}

	public void setTestLookupAlwaysId(Long testLookupAlwaysId) {
		this.testLookupAlwaysId = testLookupAlwaysId;
		this.testLookupAlways = null;
	}

	public Schedule getTestLookupAlways() {
		return testLookupAlways;
	}

	public void setTestLookupAlways(Schedule testLookupAlways) {
		this.testLookupAlways = testLookupAlways;
		this.testLookupAlwaysId = testLookupAlways != null ? testLookupAlways.getId() : null;
	}

	public Long getTestLookupWhenNullId() {
		return testLookupWhenNullId;
	}

	public void setTestLookupWhenNullId(Long testLookupWhenNullId) {
		this.testLookupWhenNullId = testLookupWhenNullId;
		this.testLookupWhenNull = null;
	}

	public Schedule getTestLookupWhenNull() {
		return testLookupWhenNull;
	}

	public void setTestLookupWhenNull(Schedule testLookupWhenNull) {
		this.testLookupWhenNull = testLookupWhenNull;
		this.testLookupWhenNullId = testLookupWhenNull != null ? testLookupWhenNull.getId() : null;
	}

	public Long getTestLookupNoneId() {
		return testLookupNoneId;
	}

	public void setTestLookupNoneId(Long testLookupNoneId) {
		this.testLookupNoneId = testLookupNoneId;
		this.testLookupNone = null;
	}

	public Schedule getTestLookupNone() {
		return testLookupNone;
	}

	public void setTestLookupNone(Schedule testLookupNone) {
		this.testLookupNone = testLookupNone;
		this.testLookupNoneId = testLookupNone != null ? testLookupNone.getId() : null;
	}

	public Long getTestSerializeEagerId() {
		return testSerializeEagerId;
	}

	public void setTestSerializeEagerId(Long testSerializeEagerId) {
		this.testSerializeEagerId = testSerializeEagerId;
		this.testSerializeEager = null;
	}

	public Schedule getTestSerializeEager() {
		return testSerializeEager;
	}

	public void setTestSerializeEager(Schedule testSerializeEager) {
		this.testSerializeEager = testSerializeEager;
		this.testSerializeEagerId = testSerializeEager != null ? testSerializeEager.getId() : null;
	}

	public Long getTestSerializeOnlyIdId() {
		return testSerializeOnlyIdId;
	}

	public void setTestSerializeOnlyIdId(Long testSerializeOnlyIdId) {
		this.testSerializeOnlyIdId = testSerializeOnlyIdId;
		this.testSerializeOnlyId = null;
	}

	public Schedule getTestSerializeOnlyId() {
		return testSerializeOnlyId;
	}

	public void setTestSerializeOnlyId(Schedule testSerializeOnlyId) {
		this.testSerializeOnlyId = testSerializeOnlyId;
		this.testSerializeOnlyIdId = testSerializeOnlyId != null ? testSerializeOnlyId.getId() : null;
	}

	public ResourceIdentifier getTestResourceIdRefId() {
		return testResourceIdRefId;
	}

	public void setTestResourceIdRefId(ResourceIdentifier testResourceIdRefId) {
		this.testResourceIdRefId = testResourceIdRefId;
		this.testResourceIdRef = null;
	}

	public Schedule getTestResourceIdRef() {
		return testResourceIdRef;
	}

	public void setTestResourceIdRef(Schedule testResourceIdRef) {
		this.testResourceIdRef = testResourceIdRef;
		this.testResourceIdRefId = testResourceIdRef != null ?
				new ResourceIdentifier(testResourceIdRef.getId().toString(), "schedules") : null;
	}

	public Long getTestSubTypedResourceId() {
		return testSubTypedResourceId;
	}

	public void setTestSubTypedResourceId(final Long testSubTypedResourceId) {
		this.testSubTypedResourceId = testSubTypedResourceId;
		this.testSubTypedResource = null;
	}

	public TopTask getTestSubTypedResource() {
		return testSubTypedResource;
	}

	public void setTestSubTypedResource(final TopTask testSubTypedResource) {
		this.testSubTypedResource = testSubTypedResource;
		this.testSubTypedResourceId = testSubTypedResource != null ? testSubTypedResource.getId() : null;
	}

	public List<TopTaskWrapper> getTopTaskWrappers() {
		return topTaskWrappers;
	}

	public void setTopTaskWrappers(final List<TopTaskWrapper> topTaskWrappers) {
		this.topTaskWrappers = topTaskWrappers;
	}
}
