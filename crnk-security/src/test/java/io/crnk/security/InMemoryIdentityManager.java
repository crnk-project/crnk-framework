package io.crnk.security;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

/**
 * A simple {@link IdentityManager} implementation, that just takes a map of users to their
 * password.
 * <p>
 * This is in now way suitable for real world production use.
 */
public class InMemoryIdentityManager {

	private ConstraintSecurityHandler securityHandler;

	private HashLoginService loginService;

	private String realm = "myrealm";

	public InMemoryIdentityManager() {
		loginService = new HashLoginService();
		loginService.setName(realm);

		securityHandler = new ConstraintSecurityHandler();
		securityHandler.setAuthenticator(new BasicAuthenticator());
		securityHandler.setRealmName(realm);
		securityHandler.setLoginService(loginService);

		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__BASIC_AUTH);
		//		constraint.setRoles(new String[] { "getRole", "postRole", "allRole" });
		constraint.setRoles(new String[]{Constraint.ANY_AUTH, "getRole", "postRole", "allRole"});
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/*");
		securityHandler.addConstraintMapping(cm);
	}

	public void addUser(String userId, String password, String... roles) {
		loginService.putUser(userId, Credential.getCredential(password), roles);

	}

	public void clear() {
		securityHandler.getConstraintMappings().clear();
	}

	public SecurityHandler getSecurityHandler() {
		return securityHandler;
	}
}
