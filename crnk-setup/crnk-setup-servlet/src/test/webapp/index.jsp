<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page language="java" %>
<html>
<head>
	<title>Crnk-Servlet Integration Example Application</title>
</head>
<body>

<h1>Crnk-Servlet Integration Example Application</h1>

<hr/>

<h3>JSON API Tests with SimpleCrnkFilter</h3>

<ul>
	<li>
		<a href="/crnk/api/v1a/tasks/">/crnk/api/v1a/tasks/</a>
	</li>
	<li>
		<a href="/crnk/api/v1a/tasks/1?filter=">/crnk/api/v1a/tasks/1?filter=</a>
	</li>
	<li>
		<a href="/crnk/api/v1a/tasks?filter=%7B%22name%22%3A%22John%22%7D">/crnk/api/v1a/tasks?filter={"name":"John"}</a>
	</li>
</ul>

<h3>JSON API Tests with SimpleCrnkServlet</h3>

<ul>
	<li>
		<a href="/crnk/api/v1b/tasks/">/crnk/api/v1b/tasks/</a>
	</li>
	<li>
		<a href="/crnk/api/v1b/tasks/1?filter=">/crnk/api/v1b/tasks/1?filter=</a>
	</li>
	<li>
		<a href="/crnk/api/v1b/tasks?filter=%7B%22name%22%3A%22John%22%7D">/crnk/api/v1b/tasks?filter={"name":"John"}</a>
	</li>
</ul>

</body>
</html>
