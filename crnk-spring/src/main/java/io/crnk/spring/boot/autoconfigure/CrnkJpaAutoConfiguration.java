package io.crnk.spring.boot.autoconfigure;

import io.crnk.jpa.JpaModule;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.spring.boot.CrnkJpaProperties;
import io.crnk.spring.boot.CrnkSpringBootProperties;
import io.crnk.spring.boot.v3.CrnkConfigV3;
import io.crnk.spring.jpa.SpringTransactionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' JPA module.
 * <p>
 * Activates when there is a bean of type {@link javax.persistence.EntityManagerFactory} and
 * {@link javax.persistence.EntityManager} on the classpath and there is no other existing
 * {@link io.crnk.jpa.JpaModule} configured.
 * <p>
 * Disable with the property <code>crnk.jpa.enabled = false</code>
 * <p>
 * This configuration class will activate <em>after</em> the Hibernate auto-configuration.
 */
@Configuration
@ConditionalOnBean({EntityManager.class, EntityManagerFactory.class})
@ConditionalOnClass(JpaModule.class)
@ConditionalOnProperty(prefix = "crnk.jpa", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(JpaModule.class)
@EnableConfigurationProperties({CrnkJpaProperties.class, CrnkSpringBootProperties.class})
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@AutoConfigureBefore
@Import({CrnkConfigV3.class})
public class CrnkJpaAutoConfiguration {

	@Autowired
	private EntityManager em;

	@Autowired
	private EntityManagerFactory emf;

	@Autowired
	private CrnkJpaProperties jpaProperties;

	@Bean
	public SpringTransactionRunner transactionRunner() {
		return new SpringTransactionRunner();
	}

	@Bean
	public JpaModule jpaModule() {
		JpaModule module = JpaModule.newServerModule(emf, em, transactionRunner());

		if (jpaProperties.getQueryFactory() != null) {
			switch (jpaProperties.getQueryFactory()) {
				case CRITERIA:
					module.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
					break;
				case QUERYDSL:
					module.setQueryFactory(QuerydslQueryFactory.newInstance());
					break;
			}
		}
		return module;
	}
}
