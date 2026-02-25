package com.company.orders.util;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

public class Logger {

    private final org.slf4j.Logger logger;

    private Logger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    public void withContext(Map<String, String> context, Runnable action) {
        try {
            context.forEach(MDC::put);
            action.run();
        } finally {
            context.keySet().forEach(MDC::remove);
        }
    }

    public <T> T withContext(Map<String, String> context, java.util.function.Supplier<T> action) {
        try {
            context.forEach(MDC::put);
            return action.get();
        } finally {
            context.keySet().forEach(MDC::remove);
        }
    }
}
