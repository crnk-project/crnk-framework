package io.crnk.example.wildfly;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/hello")
public class HelloWorldService {

	@GET
	public String sayHello() {
		return "<h1>Hello World</h1>";
	}

}