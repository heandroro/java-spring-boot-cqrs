package com.company.orders.controller;

import com.company.orders.model.CreateOrderRequest;
import com.company.orders.model.OrderDto;
import com.company.orders.model.OrderListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Order Controller API interface defining the contract for order operations.
 */
public interface OrderControllerApi {

    /**
     * Creates a new order in the system.
     *
     * @param request the order creation request
     * @param authentication the current user authentication
     * @return ResponseEntity containing the created order
     */
    ResponseEntity<OrderDto> createOrder(CreateOrderRequest request, Authentication authentication);

    /**
     * Lists orders for the authenticated user with pagination support.
     *
     * @param limit maximum number of results
     * @param offset pagination offset
     * @param status optional status filter
     * @param authentication the current user authentication
     * @return ResponseEntity containing the order list response
     */
    ResponseEntity<OrderListResponse> listOrders(Integer limit, Integer offset, String status, Authentication authentication);

    /**
     * Retrieves details of a specific order.
     *
     * @param orderId the UUID of the order
     * @param authentication the current user authentication
     * @return ResponseEntity containing the order details
     */
    ResponseEntity<OrderDto> getOrder(UUID orderId, Authentication authentication);
}
