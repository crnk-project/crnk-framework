package io.crnk.example.openliberty.microprofile;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/hello")
@Singleton
public class HelloController {

	@GET
	public String sayHello() {
		return "Hello World";
	}
}
