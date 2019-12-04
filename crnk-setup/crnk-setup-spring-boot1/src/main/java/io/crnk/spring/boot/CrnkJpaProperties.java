package io.crnk.spring.boot;

import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-data-jpa
 */
@ConfigurationProperties("crnk.jpa")
public class CrnkJpaProperties {

	/**
	 * The crnk-data-jpa query factory type to use.
	 */
	private JpaQueryFactoryType queryFactory;

	/**
	 * Whether to enable the crnk jpa auto configuration.
	 */
	private Boolean enabled = true;

	/**
	 * Whether to expose all entities as resources.
	 */
	private Boolean exposeAll = true;

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

	public Boolean getExposeAll() {
		return exposeAll;
	}

	public void setExposeAll(Boolean exposeAll) {
		this.exposeAll = exposeAll;
	}

	public enum JpaQueryFactoryType {
		/**
		 * {@link JpaCriteriaQueryFactory}
		 */
		CRITERIA,
		/**
		 * {@link QuerydslQueryFactory}
		 */
		QUERYDSL,
	}
}
