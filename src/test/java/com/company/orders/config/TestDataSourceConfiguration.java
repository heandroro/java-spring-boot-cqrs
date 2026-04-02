package com.company.orders.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Test datasource configuration.
 * Provides commandDataSource and queryDataSource beans for test profile.
 */
@Configuration
@Profile("test")
public class TestDataSourceConfiguration {

    @Primary
    @Bean(name = "commandDataSource")
    @ConfigurationProperties("spring.datasource.command")
    public DataSource commandDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "queryDataSource")
    @ConfigurationProperties("spring.datasource.query")
    public DataSource queryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }
}
