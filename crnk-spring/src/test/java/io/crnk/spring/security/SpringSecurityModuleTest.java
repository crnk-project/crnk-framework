package io.crnk.spring.security;

import org.junit.Assert;
import org.junit.Test;

public class SpringSecurityModuleTest {


	@Test
	public void checkName() {
		SpringSecurityModule module = SpringSecurityModule.create();
		Assert.assertEquals("springSecurity", module.getModuleName());
	}
}
