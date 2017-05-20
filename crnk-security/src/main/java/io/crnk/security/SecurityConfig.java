package io.crnk.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holder of {@link SecurityRule} that specify how access control is performed.
 */
public class SecurityConfig {

	private List<SecurityRule> rules = new ArrayList<>();

	private SecurityConfig(List<SecurityRule> rules) {
		this.rules = Collections.unmodifiableList(rules);
	}

	public static Builder builder() {
		return new Builder();
	}

	public List<SecurityRule> getRules() {
		return rules;
	}

	public static class Builder {

		private List<SecurityRule> rules = new ArrayList<>();

		private Builder() {
		}

		public void permitAll(ResourcePermission permission) {
			rules.add(new SecurityRule(SecurityModule.ALL_ROLE, permission));
		}

		public <T> void permitAll(Class<T> resourceClass, ResourcePermission permission) {
			permitRole(SecurityModule.ALL_ROLE, resourceClass, permission);
		}

		public void permitAll(String resourceType, ResourcePermission permission) {
			rules.add(new SecurityRule(resourceType, SecurityModule.ALL_ROLE, permission));
		}

		public void permitRole(String role, ResourcePermission permission) {
			rules.add(new SecurityRule(role, permission));
		}

		public <T> void permitRole(String role, Class<T> resourceClass, ResourcePermission permission) {
			rules.add(new SecurityRule(resourceClass, role, permission));
		}

		public void permitRole(String role, String resourceType, ResourcePermission permission) {
			rules.add(new SecurityRule(resourceType, role, permission));
		}

		public SecurityConfig build() {
			return new SecurityConfig(new ArrayList<SecurityRule>(rules));
		}
	}

}
