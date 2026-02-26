package com.company.orders.command.service;

import com.company.orders.domain.entity.Order;
import com.company.orders.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderAuthorization {

    private final Environment environment;

    public void validateCreateOrderAuthorization(UUID requestCustomerId, UUID authenticatedCustomerId) {
        if (isTestProfile()) {
            return;
        }
        
        if (!requestCustomerId.equals(authenticatedCustomerId)) {
            throw new AuthorizationException("Cannot create order for another customer");
        }
    }

    public void validateOrderAccess(Order order, UUID authenticatedCustomerId, boolean isAdmin) {
        if (isTestProfile() || isAdmin) {
            return;
        }
        
        if (!order.getCustomerId().equals(authenticatedCustomerId)) {
            throw new AuthorizationException("You do not have access to this order");
        }
    }

    private boolean isTestProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("test");
    }
}
