package io.crnk.example.jersey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.example.jersey.domain.model.Project;
import io.crnk.rs.type.JsonApiMediaType;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * NOTE that you may rather want to use crnk-client for testing! This showcases regular rest testing.
 */
public class JerseyApplicationTest extends JerseyTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	protected Application configure() {
		return new JerseyApplication();
	}

	@Test
	public void testGetProjects() throws Exception {
		Response response = target("/projects").request().get();
		assertResponseStatus(response, Response.Status.OK);
		assertHeader(response.getHeaders(), HttpHeaders.CONTENT_TYPE, JsonApiMediaType.APPLICATION_JSON_API);

		JsonNode data = mapper.readTree((InputStream) response.getEntity()).get("data");
		assertThat(data.getNodeType(), is(JsonNodeType.ARRAY));
		List<Project> projects = new ArrayList<>();
		for (JsonNode node : data) {
			projects.add(getProjectFromJson(node));
		}
		assertThat(projects, hasSize(4));
	}

	@Test
	public void testGetProject() throws Exception {
		final long projectId = 121L;
		Response response = target("/projects/" + projectId).request().get();
		assertResponseStatus(response, Response.Status.OK);
		assertHeader(response.getHeaders(), HttpHeaders.CONTENT_TYPE, JsonApiMediaType.APPLICATION_JSON_API);

		JsonNode data = mapper.readTree((InputStream) response.getEntity()).get("data");
		final Project project = getProjectFromJson(data);
		assertThat(project.getId(), is(projectId));
		assertThat(project.getName(), is("Great Project"));
	}

	private Project getProjectFromJson(JsonNode node) throws JsonProcessingException {
		if (node.isObject()) {
			ObjectNode onode = (ObjectNode) node;
			final JsonNode type = onode.remove("type");
			final JsonNode attributes = onode.remove("attributes");
			final JsonNode relationships = onode.remove("relationships");
			final JsonNode links = onode.remove("links");
			Iterator<Map.Entry<String, JsonNode>> fields = attributes.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> f = fields.next();
				onode.put(f.getKey(), f.getValue().textValue());
			}
			return mapper.treeToValue(onode, Project.class);
		} else {
			throw new JsonMappingException("Not an object: " + node);
		}
	}

	private void assertResponseStatus(Response response, Response.Status status) {
		assertThat(response, is(notNullValue()));
		assertThat(Response.Status.fromStatusCode(response.getStatus()), is(status));
	}

	private void assertHeader(MultivaluedMap<String, Object> headers, String headerName, String... headerValues) {
		assertThat(headers, hasKey(headerName));
		final List<Object> values = headers.get(headerName);
		assertThat(values, hasSize(headerValues.length));
	}
}
