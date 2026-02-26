package com.company.orders.service;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.dto.OrderListResponse;
import com.company.orders.exception.AuthorizationException;
import com.company.orders.exception.OrderException;
import com.company.orders.exception.ResourceNotFoundException;
import com.company.orders.mapper.OrderMapper;
import com.company.orders.entity.Order;
import com.company.orders.entity.OrderItem;
import com.company.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Order Service Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderMapper mapper;
    
    @Mock
    private Environment environment;

    @InjectMocks
    private OrderService service;

    private Order order;
    private OrderDto orderDto;
    private CreateOrderRequest createRequest;
    private UUID testCustomerId;
    private UUID testOrderId;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
        
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        
        order = new Order();
        order.setId(testOrderId);
        order.setCustomerId(testCustomerId);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotal(BigDecimal.valueOf(249.97));
        order.setCreatedAt(OffsetDateTime.now());

        OrderItem item = new OrderItem();
        item.setProductId("p123");
        item.setQuantity(2);
        item.setPricePerUnit(BigDecimal.valueOf(99.99));
        item.setSubtotal(BigDecimal.valueOf(199.98));
        order.addItem(item);

        orderDto = new OrderDto();
        orderDto.setId(testOrderId);
        orderDto.setCustomerId(testCustomerId);
        orderDto.setStatus("pending");
        orderDto.setTotal(BigDecimal.valueOf(249.97));

        OrderItemDto itemDto = new OrderItemDto(
            "p123",
            2,
            BigDecimal.valueOf(99.99),
            BigDecimal.valueOf(199.98)
        );

        createRequest = new CreateOrderRequest(
            testCustomerId,
            Arrays.asList(itemDto)
        );
    }

    @Test
    @DisplayName("Should create order successfully when valid input provided")
    void testCreateOrder_ShouldCreateOrder_WhenValidInput() {
        when(mapper.toEntity(createRequest)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());
        when(repository.save(any(Order.class))).thenReturn(order);
        when(mapper.toDto(order)).thenReturn(orderDto);

        OrderDto result = service.createOrder(createRequest, testCustomerId);

        assertNotNull(result);
        assertEquals(testCustomerId, result.getCustomerId());
        verify(repository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when creating order for another customer")
    void testCreateOrder_ShouldThrowException_WhenDifferentCustomer() {
        UUID differentCustomerId = UUID.randomUUID();

        assertThrows(AuthorizationException.class, 
            () -> service.createOrder(createRequest, differentCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when items are empty")
    void testCreateOrder_ShouldThrowException_WhenItemsEmpty() {
        CreateOrderRequest emptyRequest = new CreateOrderRequest(testCustomerId, Arrays.asList());

        assertThrows(OrderException.class, 
            () -> service.createOrder(emptyRequest, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void testGetOrder_ShouldReturnOrder_WhenExists() {
        when(repository.findByIdWithItems(testOrderId)).thenReturn(Optional.of(order));
        when(mapper.toDto(order)).thenReturn(orderDto);

        OrderDto result = service.getOrder(testOrderId, testCustomerId, false);

        assertNotNull(result);
        assertEquals(testOrderId, result.getId());
        verify(repository).findByIdWithItems(testOrderId);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void testGetOrder_ShouldThrowException_WhenNotFound() {
        when(repository.findByIdWithItems(testOrderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> service.getOrder(testOrderId, testCustomerId, false));
    }

    @Test
    @DisplayName("Should throw exception when user tries to access another user's order")
    void testGetOrder_ShouldThrowException_WhenUnauthorized() {
        UUID differentCustomerId = UUID.randomUUID();
        when(repository.findByIdWithItems(testOrderId)).thenReturn(Optional.of(order));

        assertThrows(AuthorizationException.class, 
            () -> service.getOrder(testOrderId, differentCustomerId, false));
    }

    @Test
    @DisplayName("Should allow admin to access any order")
    void testGetOrder_ShouldAllowAdmin_WhenDifferentCustomer() {
        UUID differentCustomerId = UUID.randomUUID();
        when(repository.findByIdWithItems(testOrderId)).thenReturn(Optional.of(order));
        when(mapper.toDto(order)).thenReturn(orderDto);

        OrderDto result = service.getOrder(testOrderId, differentCustomerId, true);

        assertNotNull(result);
        verify(repository).findByIdWithItems(testOrderId);
    }

    @Test
    @DisplayName("Should list orders with pagination")
    void testListOrders_ShouldReturnPaginatedList() {
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(repository.findByCustomerId(eq(testCustomerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(testCustomerId)).thenReturn(1L);
        when(mapper.toDtoList(anyList())).thenReturn(Arrays.asList(orderDto));

        OrderListResponse result = service.listOrders(testCustomerId, false, 20, 0, null);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals(1L, result.totalCount());
        verify(repository).findByCustomerId(eq(testCustomerId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter orders by status")
    void testListOrders_ShouldFilterByStatus() {
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(repository.findByCustomerIdAndStatus(eq(testCustomerId), any(), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerIdAndStatus(eq(testCustomerId), any())).thenReturn(1L);
        when(mapper.toDtoList(anyList())).thenReturn(Arrays.asList(orderDto));

        OrderListResponse result = service.listOrders(testCustomerId, false, 20, 0, "pending");

        assertNotNull(result);
        assertEquals(1, result.data().size());
        verify(repository).findByCustomerIdAndStatus(eq(testCustomerId), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should throw exception when item quantity is null")
    void testCreateOrder_ShouldThrowException_WhenQuantityIsNull() {
        OrderItemDto itemDto = new OrderItemDto("p123", null, BigDecimal.valueOf(99.99), null);
        CreateOrderRequest request = new CreateOrderRequest(testCustomerId, Arrays.asList(itemDto));

        when(mapper.toEntity(request)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());

        assertThrows(OrderException.class, 
            () -> service.createOrder(request, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when item quantity is less than 1")
    void testCreateOrder_ShouldThrowException_WhenQuantityLessThanOne() {
        OrderItemDto itemDto = new OrderItemDto("p123", 0, BigDecimal.valueOf(99.99), null);
        CreateOrderRequest request = new CreateOrderRequest(testCustomerId, Arrays.asList(itemDto));

        when(mapper.toEntity(request)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());

        assertThrows(OrderException.class, 
            () -> service.createOrder(request, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when item price is null")
    void testCreateOrder_ShouldThrowException_WhenPriceIsNull() {
        OrderItemDto itemDto = new OrderItemDto("p123", 2, null, null);
        CreateOrderRequest request = new CreateOrderRequest(testCustomerId, Arrays.asList(itemDto));

        when(mapper.toEntity(request)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());

        assertThrows(OrderException.class, 
            () -> service.createOrder(request, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when item price is zero or less")
    void testCreateOrder_ShouldThrowException_WhenPriceIsZeroOrLess() {
        OrderItemDto itemDto = new OrderItemDto("p123", 2, BigDecimal.ZERO, null);
        CreateOrderRequest request = new CreateOrderRequest(testCustomerId, Arrays.asList(itemDto));

        when(mapper.toEntity(request)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());

        assertThrows(OrderException.class, 
            () -> service.createOrder(request, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when item productId is null")
    void testCreateOrder_ShouldThrowException_WhenProductIdIsNull() {
        OrderItemDto itemDto = new OrderItemDto(null, 2, BigDecimal.valueOf(99.99), null);
        CreateOrderRequest request = new CreateOrderRequest(testCustomerId, Arrays.asList(itemDto));

        when(mapper.toEntity(request)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());

        assertThrows(OrderException.class, 
            () -> service.createOrder(request, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when item productId is empty")
    void testCreateOrder_ShouldThrowException_WhenProductIdIsEmpty() {
        OrderItemDto itemDto = new OrderItemDto("", 2, BigDecimal.valueOf(99.99), null);
        CreateOrderRequest request = new CreateOrderRequest(testCustomerId, Arrays.asList(itemDto));

        when(mapper.toEntity(request)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());

        assertThrows(OrderException.class, 
            () -> service.createOrder(request, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when item productId is whitespace")
    void testCreateOrder_ShouldThrowException_WhenProductIdIsWhitespace() {
        OrderItemDto itemDto = new OrderItemDto("   ", 2, BigDecimal.valueOf(99.99), null);
        CreateOrderRequest request = new CreateOrderRequest(testCustomerId, Arrays.asList(itemDto));

        when(mapper.toEntity(request)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());

        assertThrows(OrderException.class, 
            () -> service.createOrder(request, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when items list is null")
    void testCreateOrder_ShouldThrowException_WhenItemsIsNull() {
        CreateOrderRequest nullItemsRequest = new CreateOrderRequest(testCustomerId, null);

        assertThrows(OrderException.class, 
            () -> service.createOrder(nullItemsRequest, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when order total is zero or less")
    void testCreateOrder_ShouldThrowException_WhenTotalIsZeroOrLess() {
        Order orderWithZeroTotal = new Order();
        orderWithZeroTotal.setId(testOrderId);
        orderWithZeroTotal.setCustomerId(testCustomerId);
        orderWithZeroTotal.setStatus(Order.OrderStatus.PENDING);
        orderWithZeroTotal.setTotal(BigDecimal.ZERO);

        when(mapper.toEntity(createRequest)).thenReturn(orderWithZeroTotal);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());

        assertThrows(OrderException.class, 
            () -> service.createOrder(createRequest, testCustomerId));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should list all orders for admin without status filter")
    void testListOrders_AsAdminWithoutStatus() {
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);
        when(repository.count()).thenReturn(1L);
        when(mapper.toDtoList(anyList())).thenReturn(Arrays.asList(orderDto));

        OrderListResponse result = service.listOrders(testCustomerId, true, 20, 0, null);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        verify(repository).findAll(any(Pageable.class));
        verify(repository).count();
    }

    @Test
    @DisplayName("Should list all orders for admin with status filter")
    void testListOrders_AsAdminWithStatus() {
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(repository.findByStatus(any(), any(Pageable.class))).thenReturn(page);
        when(repository.count()).thenReturn(1L);
        when(mapper.toDtoList(anyList())).thenReturn(Arrays.asList(orderDto));

        OrderListResponse result = service.listOrders(testCustomerId, true, 20, 0, "pending");

        assertNotNull(result);
        assertEquals(1, result.data().size());
        verify(repository).findByStatus(any(), any(Pageable.class));
        verify(repository).count();
    }

    @Test
    @DisplayName("Should use default limit when limit is null")
    void testListOrders_WithNullLimit() {
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(repository.findByCustomerId(eq(testCustomerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(testCustomerId)).thenReturn(1L);
        when(mapper.toDtoList(anyList())).thenReturn(Arrays.asList(orderDto));

        OrderListResponse result = service.listOrders(testCustomerId, false, null, 0, null);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals(20, result.limit());
        verify(repository).findByCustomerId(eq(testCustomerId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should use default offset when offset is null")
    void testListOrders_WithNullOffset() {
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(repository.findByCustomerId(eq(testCustomerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(testCustomerId)).thenReturn(1L);
        when(mapper.toDtoList(anyList())).thenReturn(Arrays.asList(orderDto));

        OrderListResponse result = service.listOrders(testCustomerId, false, 20, null, null);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals(0, result.offset());
        verify(repository).findByCustomerId(eq(testCustomerId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should use defaults when both limit and offset are null")
    void testListOrders_WithNullLimitAndOffset() {
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(repository.findByCustomerId(eq(testCustomerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(testCustomerId)).thenReturn(1L);
        when(mapper.toDtoList(anyList())).thenReturn(Arrays.asList(orderDto));

        OrderListResponse result = service.listOrders(testCustomerId, false, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals(20, result.limit());
        assertEquals(0, result.offset());
        verify(repository).findByCustomerId(eq(testCustomerId), any(Pageable.class));
    }
}
