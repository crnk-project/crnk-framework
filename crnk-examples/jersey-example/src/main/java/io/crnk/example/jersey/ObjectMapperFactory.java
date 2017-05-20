package io.crnk.example.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.hk2.api.Factory;

public class ObjectMapperFactory implements Factory<ObjectMapper> {

	final ObjectMapper mapper = new ObjectMapper();

	@Override
	public ObjectMapper provide() {
		return mapper;
	}

	@Override
	public void dispose(ObjectMapper t) {
	}
}