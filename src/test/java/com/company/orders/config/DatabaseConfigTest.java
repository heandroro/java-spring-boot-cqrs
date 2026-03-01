package com.company.orders.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigTest {

    @Autowired
    @Qualifier("commandDataSource")
    private DataSource commandDataSource;

    @Autowired
    @Qualifier("queryDataSource")
    private DataSource queryDataSource;

    @Test
    void shouldLoadCommandDataSource() {
        assertThat(commandDataSource).isNotNull();
    }

    @Test
    void shouldLoadQueryDataSource() {
        assertThat(queryDataSource).isNotNull();
    }

    @Test
    void shouldHaveSeparateDataSources() {
        assertThat(commandDataSource).isNotSameAs(queryDataSource);
    }
}
