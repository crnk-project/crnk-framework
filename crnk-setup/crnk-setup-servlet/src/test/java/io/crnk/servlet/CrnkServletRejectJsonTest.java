/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.crnk.servlet;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.servlet.resource.repository.NodeRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonPartEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CrnkServletRejectJsonTest {
	private static final String SOME_TASK_ATTRIBUTES = "{\"name\":\"Some task\"}";

	private static final String FIRST_TASK_LINKS = "{\"self\":\"http://localhost:8080/api/tasks/1\"}";

	private static final String PROJECT1_RELATIONSHIP_LINKS =
			"{\"self\":\"http://localhost:8080/api/tasks/1/relationships/project\","
					+ "\"related\":\"http://localhost:8080/api/tasks/1/project\"}";

	private static final String RESOURCE_SEARCH_PACKAGE = "io.crnk.servlet.resource";

	private static final String RESOURCE_DEFAULT_DOMAIN = "http://localhost:8080/api";

	private static Logger log = LoggerFactory.getLogger(CrnkServletRejectJsonTest.class);

	private ServletContext servletContext;

	private CrnkServlet servlet;

	private NodeRepository nodeRepository;

	@Before
	public void before() throws Exception {
		servlet = new CrnkServlet();

		servletContext = new MockServletContext();
		((MockServletContext) servletContext).setContextPath("");
		MockServletConfig servletConfig = new MockServletConfig(servletContext);
		servletConfig
				.addInitParameter(CrnkProperties.RESOURCE_SEARCH_PACKAGE, RESOURCE_SEARCH_PACKAGE);
		servletConfig
				.addInitParameter(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, RESOURCE_DEFAULT_DOMAIN);
		servletConfig
				.addInitParameter(CrnkProperties.REJECT_PLAIN_JSON, String.valueOf(true));

		servlet.init(servletConfig);
		nodeRepository = new NodeRepository();
	}

	@After
	public void after() throws Exception {
		servlet.destroy();
		nodeRepository.clearRepo();
	}

	/**
	 * Option to reject plain JSON GET requests is enabled explicitly.
	 */
	@Test
	public void testRejectPlainJson() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath("/api");
		request.setPathInfo("/tasks");
		request.setRequestURI("/api/tasks");
		request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
		request.addHeader("Accept", "application/json");
		request.addParameter("filter[Task][name]", "John");
		request.setQueryString(URLEncoder.encode("filter[Task][name]", StandardCharsets.UTF_8.name()) + "=John");

		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.service(request, response);

		assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
		String responseContent = response.getContentAsString();
		assertTrue(responseContent == null || "".equals(responseContent.trim()));
	}

	@Test
	public void testAcceptJsonApi() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath("/api");
		request.setPathInfo("/tasks/1");
		request.setRequestURI("/api/tasks/1");
		request.addHeader("Accept", HttpHeaders.JSONAPI_CONTENT_TYPE);

		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.service(request, response);

		String responseContent = response.getContentAsString();

		log.debug("responseContent: {}", responseContent);
		assertNotNull(responseContent);

		assertJsonPartEquals("tasks", responseContent, "data.type");
		assertJsonPartEquals("\"1\"", responseContent, "data.id");
		assertJsonPartEquals(SOME_TASK_ATTRIBUTES, responseContent, "data.attributes");
		assertJsonPartEquals(FIRST_TASK_LINKS, responseContent, "data.links");
		assertJsonPartEquals(PROJECT1_RELATIONSHIP_LINKS, responseContent, "data.relationships.project.links");
	}

	@Test
	public void testAcceptWildcard() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath("/api");
		request.setPathInfo("/tasks/1");
		request.setRequestURI("/api/tasks/1");
		request.addHeader("Accept", "*/*");

		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.service(request, response);

		String responseContent = response.getContentAsString();

		log.debug("responseContent: {}", responseContent);
		assertNotNull(responseContent);

		assertJsonPartEquals("tasks", responseContent, "data.type");
		assertJsonPartEquals("\"1\"", responseContent, "data.id");
		assertJsonPartEquals(SOME_TASK_ATTRIBUTES, responseContent, "data.attributes");
		assertJsonPartEquals(FIRST_TASK_LINKS, responseContent, "data.links");
		assertJsonPartEquals(PROJECT1_RELATIONSHIP_LINKS, responseContent, "data.relationships.project.links");
	}
}
