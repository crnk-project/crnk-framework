package io.crnk.operations;

import io.crnk.operations.model.MovieEntity;
import io.crnk.spring.internal.SpringServiceDiscovery;
import io.crnk.spring.jpa.SpringTransactionRunner;
import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.dialect.H2Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan({"io.kartharsis"})
public class OperationsTestConfig {

	@Bean
	public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
		return entityManagerFactory.createEntityManager();
	}

	@Bean
	public SpringTransactionRunner transactionRunner() {
		return new SpringTransactionRunner();
	}

	@Bean
	public EntityManagerProducer entityManagerProducer() {
		return new EntityManagerProducer();
	}

	@Bean
	public SpringServiceDiscovery serviceDiscovery() {
		return new SpringServiceDiscovery();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean getLocalContainerEntityManagerFactoryBean() {
		LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
		bean.setJpaProperties(hibernateProperties());
		bean.setPackagesToScan(MovieEntity.class.getPackage().getName());
		bean.setDataSource(testDataSource());
		bean.setPersistenceUnitName("TEST");
		return bean;
	}

	@Bean
	public DataSource testDataSource() {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE");
		dataSource.setUser("sa");
		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		transactionManager.setDataSource(testDataSource());
		return transactionManager;
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	private Properties hibernateProperties() {
		Properties props = new Properties();
		props.setProperty("hibernate.dialect", H2Dialect.class.getName());
		props.setProperty("hibernate.hbm2ddl.auto", "create");
		props.setProperty("hibernate.globally_quoted_identifiers", "true");
		return props;
	}
}