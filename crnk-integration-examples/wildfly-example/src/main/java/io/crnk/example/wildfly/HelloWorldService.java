package io.crnk.example.wildfly;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/hello")
public class HelloWorldService {

	@GET
	public String sayHello() {
		return "<h1>Hello World</h1>";
	}

}