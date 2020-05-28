package io.crnk.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holder of {@link SecurityRule} that specify how access control is performed.
 */
public class SecurityConfig {

    private List<SecurityRule> rules;

    private DataRoomFilter dataRoomFilter;

    private boolean performDataRoomChecks = true;

    private boolean exposeRepositories = false;

    private SecurityConfig(List<SecurityRule> rules, DataRoomFilter dataRoomFilter) {
        this.rules = Collections.unmodifiableList(rules);
        this.dataRoomFilter = dataRoomFilter;
    }

    public boolean isExposeRepositories() {
        return exposeRepositories;
    }

    /**
     * @param exposeRepositories whether to create repositories to access the configured security rules.
     */
    public void setExposeRepositories(boolean exposeRepositories) {
        this.exposeRepositories = exposeRepositories;
    }

    /**
     * @return see {@link #setPerformDataRoomChecks(boolean)}
     */
    public boolean getPerformDataRoomChecks() {
        return performDataRoomChecks;
    }

    /**
     * @param performDataRoomChecks to add an interceptor to check all incoming requests.
     *                              application may disable this and perform security checks
     *                              manually by using {@link SecurityModule#getDataRoomMatcher()}.
     *                              Enabled by default.
     */
    public void setPerformDataRoomChecks(boolean performDataRoomChecks) {
        this.performDataRoomChecks = performDataRoomChecks;
    }

    public void setRules(List<SecurityRule> rules) {
        this.rules = rules;
    }

    public DataRoomFilter getDataRoomFilter() {
        return dataRoomFilter;
    }

    public void setDataRoomFilter(DataRoomFilter filter) {
        this.dataRoomFilter = filter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<SecurityRule> getRules() {
        return rules;
    }

    public static class Builder {

        private List<SecurityRule> rules = new ArrayList<>();

        private DataRoomFilter dataRoomFilter;

        private boolean exposeRepositories;

        private Builder() {
        }

        public Builder permitAll(ResourcePermission... permissions) {
            for (ResourcePermission permission : permissions) {
                rules.add(new SecurityRule(SecurityModule.ANY_ROLE, permission));
            }

            return this;
        }

        public <T> Builder permitAll(Class<T> resourceClass, ResourcePermission... permissions) {
            for (ResourcePermission permission : permissions) {
                permitRole(SecurityModule.ANY_ROLE, resourceClass, permission);
            }

            return this;
        }

        public Builder permitAll(String resourceType, ResourcePermission... permissions) {
            for (ResourcePermission permission : permissions) {
                rules.add(new SecurityRule(resourceType, SecurityModule.ANY_ROLE, permission));
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
            SecurityConfig config = new SecurityConfig(rules, dataRoomFilter);
            config.setExposeRepositories(exposeRepositories);
            return config;
        }

        public void setDataRoomFilter(DataRoomFilter dataRoomFilter) {
            this.dataRoomFilter = dataRoomFilter;
        }

        public Builder exposeRepositories(boolean exposeRepositories) {
            this.exposeRepositories = exposeRepositories;
            return this;
        }
    }

}
