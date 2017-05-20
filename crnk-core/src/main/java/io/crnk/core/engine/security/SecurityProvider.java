package io.crnk.core.engine.security;

public interface SecurityProvider {

	boolean isUserInRole(String role);
}
