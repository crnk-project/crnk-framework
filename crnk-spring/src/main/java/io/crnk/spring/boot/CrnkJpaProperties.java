package io.crnk.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-jpa
 */
@ConfigurationProperties("crnk.jpa")
public class CrnkJpaProperties {

	/**
	 * The crnk-jpa query factory type to use.
	 */
	private JpaQueryFactoryType queryFactory;

	/**
	 * Whether to enable the crnk jpa auto configuration.
	 */
	private Boolean enabled = true;

	public JpaQueryFactoryType getQueryFactory() {
		return queryFactory;
	}

	public void setQueryFactory(JpaQueryFactoryType queryFactory) {
		this.queryFactory = queryFactory;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public enum JpaQueryFactoryType {
		/**
		 * {@link io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory}
		 */
		CRITERIA,
		/**
		 * {@link io.crnk.jpa.query.querydsl.QuerydslQueryFactory}
		 */
		QUERYDSL,
	}
}
