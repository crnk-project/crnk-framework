package io.crnk.spring.boot.autoconfigure;

import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.spring.boot.CrnkJpaProperties;
import io.crnk.spring.boot.CrnkSpringBootProperties;
import io.crnk.spring.boot.JpaModuleConfigurer;
import io.crnk.spring.boot.v3.CrnkConfigV3;
import io.crnk.spring.jpa.SpringTransactionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' JPA module.
 * <p>
 * Activates when there is a bean of type {@link jakarta.persistence.EntityManagerFactory} and
 * {@link jakarta.persistence.EntityManager} on the classpath and there is no other existing
 * {@link JpaModule} configured.
 * <p>
 * Disable with the property <code>crnk.jpa.enabled = false</code>. By default all entities are exposed.
 * <p>
 * This configuration class will activate <em>after</em> the Hibernate auto-configuration.
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.jpa", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(JpaModule.class)
@ConditionalOnMissingBean(JpaModule.class)
@EnableConfigurationProperties({CrnkJpaProperties.class, CrnkSpringBootProperties.class})
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@AutoConfigureBefore
@Import({CrnkConfigV3.class})
public class CrnkJpaAutoConfiguration {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private CrnkJpaProperties jpaProperties;

    @Autowired(required = false)
    private List<JpaModuleConfigurer> configurers;


    @Bean
    @ConditionalOnMissingBean
    public SpringTransactionRunner transactionRunner() {
        return new SpringTransactionRunner();
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaModuleConfig jpaModuleConfig() {
        return new JpaModuleConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaModule jpaModule(JpaModuleConfig config) {
        if (configurers != null) {
            for (JpaModuleConfigurer configurer : configurers) {
                configurer.configure(config);
            }
        }

        if (jpaProperties.getExposeAll()) {
            config.exposeAllEntities(emf);
        }

        if (jpaProperties.getQueryFactory() != null) {
            switch (jpaProperties.getQueryFactory()) {
                case CRITERIA:
                    config.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
                    break;
                case QUERYDSL:
                    config.setQueryFactory(QuerydslQueryFactory.newInstance());
                    break;
                default:
                    throw new IllegalStateException("unknown query factory");
            }
        }
        return JpaModule.createServerModule(config, em, transactionRunner());
    }
}
