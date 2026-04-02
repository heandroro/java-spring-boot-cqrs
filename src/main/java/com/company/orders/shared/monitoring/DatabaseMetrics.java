package com.company.orders.shared.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DatabaseMetrics {

    @Qualifier("commandDataSource")
    private final DataSource commandDataSource;

    @Qualifier("queryDataSource")
    private final DataSource queryDataSource;

    public Map<String, Object> getReplicationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try (Connection conn = queryDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if replica
            ResultSet rs = stmt.executeQuery("SELECT pg_is_in_recovery()");
            if (rs.next()) {
                metrics.put("is_replica", rs.getBoolean(1));
            }
            rs.close();

            // Get replication lag
            rs = stmt.executeQuery(
                "SELECT EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp())) AS lag_seconds"
            );
            if (rs.next()) {
                double lagSeconds = rs.getDouble("lag_seconds");
                metrics.put("replication_lag_seconds", lagSeconds);
                metrics.put("replication_lag_ms", lagSeconds * 1000);
            }
            rs.close();

            metrics.put("status", "healthy");
            
        } catch (Exception e) {
            log.error("Error getting replication metrics", e);
            metrics.put("status", "error");
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    public Map<String, Object> getConnectionPoolMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Command datasource
            try (Connection conn = commandDataSource.getConnection()) {
                metrics.put("command_datasource_connected", true);
            }
            
            // Query datasource
            try (Connection conn = queryDataSource.getConnection()) {
                metrics.put("query_datasource_connected", true);
            }
            
            metrics.put("status", "healthy");
            
        } catch (Exception e) {
            log.error("Error getting connection pool metrics", e);
            metrics.put("status", "error");
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    public void logReplicationStatus() {
        Map<String, Object> metrics = getReplicationMetrics();
        log.info("Replication Status: {}", metrics);
    }
}
