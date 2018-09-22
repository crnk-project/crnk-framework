package io.crnk.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holder of {@link SecurityRule} that specify how access control is performed.
 */
public class SecurityConfig {

	private List<SecurityRule> rules;

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

		public Builder permitAll(ResourcePermission... permissions) {
			for (ResourcePermission permission : permissions) {
				rules.add(new SecurityRule(SecurityModule.ALL_ROLE, permission));
			}

			return this;
		}

		public <T> Builder permitAll(Class<T> resourceClass, ResourcePermission... permissions) {
			for (ResourcePermission permission : permissions) {
				permitRole(SecurityModule.ALL_ROLE, resourceClass, permission);
			}

			return this;
		}

		public Builder permitAll(String resourceType, ResourcePermission... permissions) {
			for (ResourcePermission permission : permissions) {
				rules.add(new SecurityRule(resourceType, SecurityModule.ALL_ROLE, permission));
			}

			return this;
		}

		public Builder permitRole(String role, ResourcePermission... permissions) {
			for (ResourcePermission permission : permissions) {
				rules.add(new SecurityRule(role, permission));
			}

			return this;
		}

		public <T> Builder permitRole(String role, Class<T> resourceClass, ResourcePermission... permissions) {
			for (ResourcePermission permission : permissions) {
				rules.add(new SecurityRule(resourceClass, role, permission));
			}

			return this;
		}

		public Builder permitRole(String role, String resourceType, ResourcePermission... permissions) {
			for (ResourcePermission permission : permissions) {
				rules.add(new SecurityRule(resourceType, role, permission));
			}

			return this;
		}

		public SecurityConfig build() {
			return new SecurityConfig(rules);
		}
	}

}
