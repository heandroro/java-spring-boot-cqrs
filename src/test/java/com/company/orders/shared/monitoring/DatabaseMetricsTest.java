package com.company.orders.shared.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseMetricsTest {

    @Mock
    private DataSource commandDataSource;

    @Mock
    private DataSource queryDataSource;

    @Mock
    private Connection commandConnection;

    @Mock
    private Connection queryConnection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    private DatabaseMetrics databaseMetrics;

    @BeforeEach
    void setUp() throws Exception {
        databaseMetrics = new DatabaseMetrics(commandDataSource, queryDataSource);
    }

    @Test
    void shouldGetReplicationMetricsSuccessfully() throws Exception {
        // Given
        when(queryDataSource.getConnection()).thenReturn(queryConnection);
        when(queryConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(1)).thenReturn(true); // is_replica = true
        when(resultSet.getDouble("lag_seconds")).thenReturn(0.123);

        // When
        Map<String, Object> metrics = databaseMetrics.getReplicationMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.get("is_replica")).isEqualTo(true);
        assertThat(metrics.get("replication_lag_seconds")).isEqualTo(0.123);
        assertThat(metrics.get("replication_lag_ms")).isEqualTo(123.0);
        assertThat(metrics.get("status")).isEqualTo("healthy");

        verify(statement, times(2)).executeQuery(anyString());
        verify(resultSet, times(2)).close();
        verify(queryConnection).close();
    }

    @Test
    void shouldHandleReplicationMetricsError() throws Exception {
        // Given
        when(queryDataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        // When
        Map<String, Object> metrics = databaseMetrics.getReplicationMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.get("status")).isEqualTo("error");
        assertThat(metrics.get("error")).isEqualTo("Connection failed");
    }

    @Test
    void shouldGetConnectionPoolMetricsSuccessfully() throws Exception {
        // Given
        when(commandDataSource.getConnection()).thenReturn(commandConnection);
        when(queryDataSource.getConnection()).thenReturn(queryConnection);

        // When
        Map<String, Object> metrics = databaseMetrics.getConnectionPoolMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.get("command_datasource_connected")).isEqualTo(true);
        assertThat(metrics.get("query_datasource_connected")).isEqualTo(true);
        assertThat(metrics.get("status")).isEqualTo("healthy");

        verify(commandConnection).close();
        verify(queryConnection).close();
    }

    @Test
    void shouldHandleConnectionPoolMetricsError() throws Exception {
        // Given
        when(commandDataSource.getConnection()).thenThrow(new RuntimeException("Command connection failed"));
        // Note: queryDataSource.getConnection() is not stubbed - it won't be called due to exception

        // When
        Map<String, Object> metrics = databaseMetrics.getConnectionPoolMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.get("status")).isEqualTo("error");
        assertThat(metrics.get("error")).isEqualTo("Command connection failed");
    }

    @Test
    void shouldLogReplicationStatus() throws Exception {
        // Given - mock successful replication metrics
        when(queryDataSource.getConnection()).thenReturn(queryConnection);
        when(queryConnection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(1)).thenReturn(false); // is_replica = false
        when(resultSet.getDouble("lag_seconds")).thenReturn(0.0);

        // When
        databaseMetrics.logReplicationStatus();

        // Then - method should execute without error
        verify(statement, times(2)).executeQuery(anyString());
    }
}
