package io.crnk.gen.typescript.model.libraries;

import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSSource;

public class NgrxJsonApiLibrary {

	public static final TSInterfaceType STORE_RESOURCE;

	public static final TSInterfaceType RESOURCE_RELATIONSHIP;

	public static final TSInterfaceType MANY_RESOURCE_RELATIONSHIP;

	public static final TSInterfaceType ONE_RESOURCE_RELATIONSHIP;

	public static final TSClassType TYPED_ONE_RESOURCE_RELATIONSHIP;

	public static final TSClassType TYPED_MANY_RESOURCE_RELATIONSHIP;

	public static final TSInterfaceType ONE_QUERY_RESULT;

	public static final TSInterfaceType MANY_QUERY_RESULT;

	static {
		TSSource ngrxJsonApiSource = new TSSource();
		ngrxJsonApiSource.setNpmPackage("ngrx-json-api");

		// TODO for some reason the fields in StoreResource resp. ResourceIdentifier do not get recoginized by
		// Typescript compiler in application.s
		STORE_RESOURCE = new TSInterfaceType();
		STORE_RESOURCE.setName("CrnkStoreResource");
		STORE_RESOURCE.setParent(CrnkLibrary.STUB_SOURCE);

		RESOURCE_RELATIONSHIP = new TSInterfaceType();
		RESOURCE_RELATIONSHIP.setName("ResourceRelationship");
		RESOURCE_RELATIONSHIP.setParent(ngrxJsonApiSource);

		MANY_RESOURCE_RELATIONSHIP = new TSInterfaceType();
		MANY_RESOURCE_RELATIONSHIP.setName("ManyResourceRelationship");
		MANY_RESOURCE_RELATIONSHIP.setParent(ngrxJsonApiSource);

		ONE_RESOURCE_RELATIONSHIP = new TSInterfaceType();
		ONE_RESOURCE_RELATIONSHIP.setName("OneResourceRelationship");
		ONE_RESOURCE_RELATIONSHIP.setParent(ngrxJsonApiSource);

		TYPED_ONE_RESOURCE_RELATIONSHIP = new TSClassType();
		TYPED_ONE_RESOURCE_RELATIONSHIP.setName("TypedOneResourceRelationship");
		TYPED_ONE_RESOURCE_RELATIONSHIP.setParent(ngrxJsonApiSource);

		TYPED_MANY_RESOURCE_RELATIONSHIP = new TSClassType();
		TYPED_MANY_RESOURCE_RELATIONSHIP.setName("TypedManyResourceRelationship");
		TYPED_MANY_RESOURCE_RELATIONSHIP.setParent(ngrxJsonApiSource);

		ONE_QUERY_RESULT = new TSInterfaceType();
		ONE_QUERY_RESULT.setName("OneQueryResult");
		ONE_QUERY_RESULT.setParent(ngrxJsonApiSource);

		MANY_QUERY_RESULT = new TSInterfaceType();
		MANY_QUERY_RESULT.setName("ManyQueryResult");
		MANY_QUERY_RESULT.setParent(ngrxJsonApiSource);
	}

	private NgrxJsonApiLibrary() {
	}

}
