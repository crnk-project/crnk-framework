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
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.*;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonPartEquals;
import static org.junit.Assert.assertNotNull;

public class CrnkFilterTest {

	private static final String FIRST_TASK_ATTRIBUTES = "{\"name\":\"First task\"}";

	private static final String SOME_TASK_ATTRIBUTES = "{\"name\":\"Some task\"}";

	private static final String FIRST_TASK_LINKS = "{\"self\":\"http://localhost:8080/api/tasks/1\"}";

	private static final String PROJECT1_RELATIONSHIP_LINKS =
			"{\"self\":\"http://localhost:8080/api/tasks/1/relationships/project\","
					+ "\"related\":\"http://localhost:8080/api/tasks/1/project\"}";
	private static final String RESOURCE_SEARCH_PACKAGE = "io.crnk.servlet.resource";
	private static final String RESOURCE_DEFAULT_DOMAIN = "http://localhost:8080";
	private static Logger log = LoggerFactory.getLogger(CrnkFilterTest.class);
	private ServletContext servletContext;

	private FilterConfig filterConfig;

	private Filter filter;

	@Before
	public void before() throws Exception {
		filter = new CrnkFilter();

		servletContext = new MockServletContext();
		((MockServletContext) servletContext).setContextPath("");
		filterConfig = new MockFilterConfig(servletContext);
		((MockFilterConfig) filterConfig).addInitParameter(CrnkProperties.WEB_PATH_PREFIX, "/api");
		((MockFilterConfig) filterConfig).addInitParameter(CrnkProperties.RESOURCE_SEARCH_PACKAGE, RESOURCE_SEARCH_PACKAGE);
		((MockFilterConfig) filterConfig).addInitParameter(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, RESOURCE_DEFAULT_DOMAIN);

		filter.init(filterConfig);
	}

	@After
	public void after() throws Exception {
		filter.destroy();
	}

	@Test
	public void onSimpleCollectionGetShouldReturnCollectionOfResources() throws Exception {
		MockFilterChain filterChain = new MockFilterChain();

		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath(null);
		request.setPathInfo(null);
		request.setRequestURI("/api/tasks/");
		request.setContentType(JsonApiRequestProcessor.JSONAPI_CONTENT_TYPE);
		request.addHeader("Accept", "*/*");

		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, filterChain);

		Assert.assertEquals(200, response.getStatus());

		String responseContent = response.getContentAsString();

		log.debug("responseContent: {}", responseContent);
		assertNotNull(responseContent);

		assertJsonPartEquals("tasks", responseContent, "data[0].type");
		assertJsonPartEquals("\"1\"", responseContent, "data[0].id");
		assertJsonPartEquals(FIRST_TASK_ATTRIBUTES, responseContent, "data[0].attributes");
		assertJsonPartEquals(FIRST_TASK_LINKS, responseContent, "data[0].links");
		assertJsonPartEquals(PROJECT1_RELATIONSHIP_LINKS, responseContent, "data[0].relationships.project.links");
	}

	@Test
	public void onSimpleResourceGetShouldReturnOneResource() throws Exception {
		MockFilterChain filterChain = new MockFilterChain();

		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath(null);
		request.setPathInfo(null);
		request.setRequestURI("/api/tasks/1");
		request.setContentType(JsonApiRequestProcessor.JSONAPI_CONTENT_TYPE);
		request.addHeader("Accept", "*/*");

		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, filterChain);

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
	public void onCollectionRequestWithParamsGetShouldReturnCollection() throws Exception {
		MockFilterChain filterChain = new MockFilterChain();

		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath(null);
		request.setPathInfo(null);
		request.setRequestURI("/api/tasks");
		request.setContentType(JsonApiRequestProcessor.JSONAPI_CONTENT_TYPE);
		request.addHeader("Accept", "*/*");
		request.addParameter("filter[name]", "John");
		request.setQueryString(URLEncoder.encode("filter[name]", StandardCharsets.UTF_8.name()) + "=John");

		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, filterChain);

		String responseContent = response.getContentAsString();

		log.debug("responseContent: {}", responseContent);
		assertNotNull(responseContent);

		assertJsonPartEquals("tasks", responseContent, "data[0].type");
		assertJsonPartEquals("\"1\"", responseContent, "data[0].id");
		assertJsonPartEquals(FIRST_TASK_ATTRIBUTES, responseContent, "data[0].attributes");
		assertJsonPartEquals(FIRST_TASK_LINKS, responseContent, "data[0].links");
		assertJsonPartEquals(PROJECT1_RELATIONSHIP_LINKS, responseContent, "data[0].relationships.project.links");
	}
}
