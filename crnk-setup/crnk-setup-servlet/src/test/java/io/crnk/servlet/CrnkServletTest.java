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
import io.crnk.core.repository.ResourceRepository;
import io.crnk.servlet.resource.model.Locale;
import io.crnk.servlet.resource.model.Node;
import io.crnk.servlet.resource.model.NodeComment;
import io.crnk.servlet.resource.model.ServletTestModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonNodePresent;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonPartEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CrnkServletTest {

    private static final String FIRST_TASK_ATTRIBUTES = "{\"name\":\"First task\"}";

    private static final String SOME_TASK_ATTRIBUTES = "{\"name\":\"Some task\"}";

    private static final String FIRST_TASK_LINKS = "{\"self\":\"http://localhost:8080/api/tasks/1\"}";

    private static final String PROJECT1_RELATIONSHIP_LINKS =
            "{\"self\":\"http://localhost:8080/api/tasks/1/relationships/project\","
                    + "\"related\":\"http://localhost:8080/api/tasks/1/project\"}";

    private static final String RESOURCE_SEARCH_PACKAGE = "io.crnk.servlet.resource";

    private static final String RESOURCE_DEFAULT_DOMAIN = "http://localhost:8080/api";

    private static Logger log = LoggerFactory.getLogger(CrnkServletTest.class);

    private ServletContext servletContext;

    private ServletConfig servletConfig;

    private CrnkServlet servlet;


    @Before
    public void before() throws Exception {
        servlet = new CrnkServlet();
        servlet.getBoot().addModule(new ServletTestModule());

        servletContext = new MockServletContext();
        ((MockServletContext) servletContext).setContextPath("");
        servletConfig = new MockServletConfig(servletContext);
        ((MockServletConfig) servletConfig)
                .addInitParameter(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, RESOURCE_DEFAULT_DOMAIN);

        servlet.init(servletConfig);
    }

    @After
    public void after() {
        servlet.destroy();
    }

    @Test
    public void testGetBoot() {
        Assert.assertNotNull(servlet.getBoot());
    }

    @Test
    public void onSimpleCollectionGetShouldReturnCollectionOfResources() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks/");
        request.setRequestURI("/api/tasks/");
        request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
        request.addHeader("Accept", "*/*");

        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

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
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks/1");
        request.setRequestURI("/api/tasks/1");
        request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
        request.addHeader("Accept", "*/*");

        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        String responseContent = response.getContentAsString();

        log.debug("responseContent: {}", responseContent);
        assertNotNull(responseContent);

        assertJsonPartEquals("tasks", responseContent, "data.type");
        assertJsonPartEquals("\"1\"", responseContent, "data.id");
        assertJsonPartEquals(FIRST_TASK_ATTRIBUTES, responseContent, "data.attributes");
        assertJsonPartEquals(FIRST_TASK_LINKS, responseContent, "data.links");
        assertJsonPartEquals(PROJECT1_RELATIONSHIP_LINKS, responseContent, "data.relationships.project.links");
    }


    @Test
    public void testAcceptPlainJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks/1");
        request.setRequestURI("/api/tasks/1");
        request.addHeader("Accept", "application/json");

        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        String responseContent = response.getContentAsString();

        log.debug("responseContent: {}", responseContent);
        assertNotNull(responseContent);

        assertJsonPartEquals("tasks", responseContent, "data.type");
        assertJsonPartEquals("\"1\"", responseContent, "data.id");
        assertJsonPartEquals(FIRST_TASK_ATTRIBUTES, responseContent, "data.attributes");
        assertJsonPartEquals(FIRST_TASK_LINKS, responseContent, "data.links");
        assertJsonPartEquals(PROJECT1_RELATIONSHIP_LINKS, responseContent, "data.relationships.project.links");
    }

    @Test
    public void onCollectionRequestWithParamsGetShouldReturnCollection() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks");
        request.setRequestURI("/api/tasks");
        request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
        request.addHeader("Accept", "*/*");
        request.addParameter("filter[name]", "First task");
        request.setQueryString(URLEncoder.encode("filter[name]", StandardCharsets.UTF_8.name()) + "=First task");

        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

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
    public void testUnacceptableRequestContentType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks");
        request.setRequestURI("/api/tasks");
        request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
        request.addHeader("Accept", "application/xml");
        request.addParameter("filter[Task][name]", "John");
        request.setQueryString(URLEncoder.encode("filter[Task][name]", StandardCharsets.UTF_8.name()) + "=John");

        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
        String responseContent = response.getContentAsString();
        assertTrue(responseContent == null || "".equals(responseContent.trim()));
    }

    @Test
    public void testMatchingException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks-matching-exception");
        request.setRequestURI("/api/matching-exception");
        request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
        request.addHeader("Accept", "*/*");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        assertEquals(404, response.getStatus());

    }

    @Test
    public void testInclude() throws Exception {

        Node root = new Node(1L, null, null);
        Node child1 = new Node(2L, root, Collections.emptySet());
        Node child2 = new Node(3L, root, Collections.emptySet());
        root.setChildren(new LinkedHashSet<>(Arrays.asList(child1, child2)));

        ResourceRepository nodeRepository = (ResourceRepository) servlet.getBoot().getResourceRegistry()
                .getEntry(Node.class).getResourceRepository().getImplementation();
        nodeRepository.save(root);
        nodeRepository.save(child1);
        nodeRepository.save(child2);
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setServletPath("/api");
        request.setPathInfo("/nodes/1");
        request.setRequestURI("/api/nodes/1");
        request.setQueryString("include[nodes]=parent");
        request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
        Map<String, String> params = new HashMap<>();
        params.put("include[nodes]", "children");
        request.setParameters(params);
        request.addHeader("Accept", "*/*");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        String responseContent = response.getContentAsString();
        assertTopLevelNodesCorrectWithChildren(responseContent);
    }

    @Test
    public void testIncludeNestedWithDefault() throws Exception {
        Node root = new Node(1L, null, null);
        Locale engLocale = new Locale(1L, java.util.Locale.ENGLISH);
        Node child1 = new Node(2L, root, Collections.emptySet());
        NodeComment child1Comment = new NodeComment(1L, "Child 1", child1, engLocale);
        child1.setNodeComments(new LinkedHashSet<>(Collections.singleton(child1Comment)));
        Node child2 = new Node(3L, root, Collections.emptySet(), Collections.emptySet());
        root.setChildren(new LinkedHashSet<>(Arrays.asList(child1, child2)));

        ResourceRepository nodeRepository = (ResourceRepository) servlet.getBoot().getResourceRegistry()
                .getEntry(Node.class).getResourceRepository().getImplementation();
        nodeRepository.save(root);
        nodeRepository.save(child1);
        nodeRepository.save(child2);

        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setServletPath("/api");
        request.setPathInfo("/nodes/1");
        request.setRequestURI("/api/nodes/1");
        request.setQueryString("include[nodes]=children.nodeComments");
        request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
        Map<String, String> params = new HashMap<>();
        params.put("include[nodes]", "children.nodeComments.langLocale");
        request.setParameters(params);
        request.addHeader("Accept", "*/*");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        String responseContent = response.getContentAsString();
        assertTopLevelNodesCorrectWithChildren(responseContent);
    }

    private void assertTopLevelNodesCorrectWithChildren(String responseContent) {
        assertJsonNodePresent(responseContent, "data.relationships.children.data");
        assertJsonPartEquals("nodes", responseContent, "data.type");
        assertJsonPartEquals("\"1\"", responseContent, "data.id");
        assertJsonPartEquals("\"2\"", responseContent, "data.relationships.children.data[0].id");
        assertJsonPartEquals("\"3\"", responseContent, "data.relationships.children.data[1].id");
    }
}
