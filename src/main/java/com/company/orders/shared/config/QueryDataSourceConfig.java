package com.company.orders.shared.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.company.orders.query.repository",
        entityManagerFactoryRef = "queryEntityManagerFactory",
        transactionManagerRef = "queryTransactionManager"
)
@org.springframework.context.annotation.Profile("!test")
public class QueryDataSourceConfig {

    @Bean(name = "queryDataSource")
    @ConfigurationProperties("spring.datasource.query")
    public DataSource queryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "queryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean queryEntityManagerFactory(
            @Qualifier("queryDataSource") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.company.orders.domain.entity");
        em.setPersistenceUnitName("query");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.use_sql_comments", true);
        em.setJpaPropertyMap(properties);
        
        return em;
    }

    @Bean(name = "queryTransactionManager")
    public PlatformTransactionManager queryTransactionManager(
            @Qualifier("queryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
        transactionManager.setDefaultTimeout(30);
        return transactionManager;
    }
}
