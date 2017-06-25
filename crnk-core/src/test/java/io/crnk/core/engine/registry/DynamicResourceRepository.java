package io.crnk.core.engine.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.repository.UntypedResourceRepository;
import io.crnk.core.resource.list.DefaultResourceList;

import java.io.IOException;
import java.util.Arrays;


public class DynamicResourceRepository extends ResourceRepositoryBase<Resource, String> implements UntypedResourceRepository<Resource, String> {


	public DynamicResourceRepository() {
		super(Resource.class);
	}

	@Override
	public String getResourceType() {
		return "dynamic";
	}

	@Override
	public Class<Resource> getResourceClass() {
		return Resource.class;
	}

	@Override
	public DefaultResourceList<Resource> findAll(QuerySpec querySpec) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Resource resource = new Resource();
			resource.setId("john");
			resource.setType("dynamic");
			resource.getAttributes().put("value", mapper.readTree("\"doe\""));
			return querySpec.apply(Arrays.asList(resource));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}