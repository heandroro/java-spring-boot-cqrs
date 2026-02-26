package com.company.orders.shared.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Logger Utility Tests")
class LoggerTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;
    private ch.qos.logback.classic.Logger logbackLogger;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger(LoggerTest.class);
        
        logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LoggerTest.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);
        logbackLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        logbackLogger.detachAppender(listAppender);
        MDC.clear();
    }

    @Test
    @DisplayName("Should create logger instance for a class")
    void getLogger() {
        Logger testLogger = Logger.getLogger(String.class);
        assertNotNull(testLogger);
    }

    @Test
    @DisplayName("Should log info message")
    void logInfo() {
        logger.info("Test info message");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals("Test info message", logsList.get(0).getMessage());
    }

    @Test
    @DisplayName("Should log info message with arguments")
    void logInfoWithArgs() {
        logger.info("Test info message with args: {} and {}", "arg1", "arg2");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertTrue(logsList.get(0).getFormattedMessage().contains("arg1"));
        assertTrue(logsList.get(0).getFormattedMessage().contains("arg2"));
    }

    @Test
    @DisplayName("Should log debug message")
    void logDebug() {
        logger.debug("Test debug message");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.DEBUG, logsList.get(0).getLevel());
        assertEquals("Test debug message", logsList.get(0).getMessage());
    }

    @Test
    @DisplayName("Should log debug message with arguments")
    void logDebugWithArgs() {
        logger.debug("Test debug message with args: {}", "arg1");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.DEBUG, logsList.get(0).getLevel());
        assertTrue(logsList.get(0).getFormattedMessage().contains("arg1"));
    }

    @Test
    @DisplayName("Should log warn message")
    void logWarn() {
        logger.warn("Test warn message");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.WARN, logsList.get(0).getLevel());
        assertEquals("Test warn message", logsList.get(0).getMessage());
    }

    @Test
    @DisplayName("Should log warn message with arguments")
    void logWarnWithArgs() {
        logger.warn("Test warn message with args: {}", "arg1");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.WARN, logsList.get(0).getLevel());
        assertTrue(logsList.get(0).getFormattedMessage().contains("arg1"));
    }

    @Test
    @DisplayName("Should log error message with throwable")
    void logErrorWithThrowable() {
        Throwable throwable = new RuntimeException("Test exception");
        logger.error("Test error message", throwable);
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        assertEquals("Test error message", logsList.get(0).getMessage());
        assertNotNull(logsList.get(0).getThrowableProxy());
    }

    @Test
    @DisplayName("Should log error message with arguments")
    void logErrorWithArgs() {
        logger.error("Test error message with args: {}", "arg1");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        assertTrue(logsList.get(0).getFormattedMessage().contains("arg1"));
    }

    @Test
    @DisplayName("Should execute Runnable with MDC context")
    void withContextRunnable() {
        Map<String, String> context = new HashMap<>();
        context.put("traceId", "12345");
        context.put("userId", "user123");

        logger.withContext(context, () -> {
            assertEquals("12345", MDC.get("traceId"));
            assertEquals("user123", MDC.get("userId"));
            logger.info("Test message with context");
        });

        assertNull(MDC.get("traceId"));
        assertNull(MDC.get("userId"));
    }

    @Test
    @DisplayName("Should execute Supplier with MDC context and return value")
    void withContextSupplier() {
        Map<String, String> context = new HashMap<>();
        context.put("traceId", "67890");

        String result = logger.withContext(context, () -> {
            assertEquals("67890", MDC.get("traceId"));
            return "test-result";
        });

        assertEquals("test-result", result);
        assertNull(MDC.get("traceId"));
    }

    @Test
    @DisplayName("Should clear MDC context even if Runnable throws exception")
    void withContextRunnableThrowsException() {
        Map<String, String> context = new HashMap<>();
        context.put("traceId", "error-trace");

        assertThrows(RuntimeException.class, () -> {
            logger.withContext(context, () -> {
                throw new RuntimeException("Test exception");
            });
        });

        assertNull(MDC.get("traceId"));
    }

    @Test
    @DisplayName("Should clear MDC context even if Supplier throws exception")
    void withContextSupplierThrowsException() {
        Map<String, String> context = new HashMap<>();
        context.put("traceId", "error-trace");

        assertThrows(RuntimeException.class, () -> {
            logger.withContext(context, () -> {
                throw new RuntimeException("Test exception");
            });
        });

        assertNull(MDC.get("traceId"));
    }
}
