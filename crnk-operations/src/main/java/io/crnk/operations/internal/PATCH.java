package io.crnk.operations.internal;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.NameBinding;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("PATCH")
@Documented
@NameBinding
public @interface PATCH {

}
