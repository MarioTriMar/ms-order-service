package com.tfm.ms_order_service;

import com.tfm.ms_order_service.model.*;
import com.tfm.ms_order_service.repository.OrderRepository;
import com.tfm.ms_order_service.repository.RecurrentOrderRepository;
import com.tfm.ms_order_service.service.OrderService;
import com.tfm.ms_order_service.service.restTemplate.ProductRestTemplate;
import com.tfm.ms_order_service.service.restTemplate.UserRestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Any;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceTest {
    @Mock private UserRestTemplate userRestTemplate;
    @Mock private ProductRestTemplate productRestTemplate;
    @Mock private OrderRepository orderRepository;
    @Mock private KafkaTemplate<String, Order> kafkaTemplate;
    @Mock private RecurrentOrderRepository recurrentOrderRepository;
    @InjectMocks
    OrderService orderService;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetTotalExpendedByUser(){
        List<Order> orders = getAuxOrders();
        when(orderRepository.findByUserId(anyString())).thenReturn(orders);
        assertEquals(35, orderService.getTotalExpendedByUser("1"));
    }
    @Test
    void shouldReturnBadRequestWhenUserDoesNotExist() {
        OrderDTO dto = new OrderDTO();
        dto.setUserId("user-123");

        when(userRestTemplate.existUser("user-123")).thenReturn(null);

        ResponseEntity<?> response = orderService.createOrder(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User doesn't exist", response.getBody());
    }

    @Test
    void shouldReturnBadGatewayWhenProductServiceFails() {
        OrderDTO dto = new OrderDTO();
        dto.setUserId("user-123");

        when(userRestTemplate.existUser("user-123")).thenReturn(new UserOrder());
        when(productRestTemplate.productsAreOk(any())).thenReturn(null);

        ResponseEntity<?> response = orderService.createOrder(dto);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Error in httpRequest", response.getBody());
    }

    @Test
    void shouldReturnBadRequestWhenProductsOutOfStock() {
        OrderDTO dto = new OrderDTO();
        dto.setUserId("user-123");

        ProductResponse outOfStockProduct = new ProductResponse();
        outOfStockProduct.setName("Product A");
        outOfStockProduct.setStockAllow(false);

        ListProductResponse listProductResponse = new ListProductResponse();
        listProductResponse.setProductResponse(List.of(outOfStockProduct));

        when(userRestTemplate.existUser("user-123")).thenReturn(new UserOrder());
        when(productRestTemplate.productsAreOk(any())).thenReturn(listProductResponse);

        ResponseEntity<?> response = orderService.createOrder(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Product A"));
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        OrderDTO dto = new OrderDTO();
        dto.setUserId("user-123");
        dto.setDeliveryDir("Calle Falsa 123");
        dto.setRecurrent(false);
        dto.setMakeRecurrent(false);

        ProductResponse product = new ProductResponse();
        product.setId("prod-1");
        product.setName("Product A");
        product.setCompanyName("Company A");
        product.setPrice(10.0);
        product.setQuantity(2);
        product.setStockAllow(true);

        ListProductResponse listProductResponse = new ListProductResponse();
        listProductResponse.setProductResponse(List.of(product));

        UserOrder userOrder = new UserOrder();
        userOrder.setId("user-123");

        Order savedOrder = new Order();
        savedOrder.setId("order-999");

        when(userRestTemplate.existUser("user-123")).thenReturn(userOrder);
        when(productRestTemplate.productsAreOk(any())).thenReturn(listProductResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        ResponseEntity<?> response = orderService.createOrder(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("order-999 created."));
        verify(kafkaTemplate).send(eq("email-service-topic"), eq("order-999"), any(Order.class));
    }

    @Test
    void shouldSaveRecurrentOrderWhenMakeRecurrentIsTrue() {
        OrderDTO dto = new OrderDTO();
        dto.setUserId("user-123");
        dto.setDeliveryDir("Av. Siempre Viva 742");
        dto.setMakeRecurrent(true);

        ProductResponse product = new ProductResponse();
        product.setId("prod-1");
        product.setName("Product A");
        product.setCompanyName("Company A");
        product.setPrice(20.0);
        product.setQuantity(1);
        product.setStockAllow(true);

        ListProductResponse listProductResponse = new ListProductResponse();
        listProductResponse.setProductResponse(List.of(product));

        UserOrder userOrder = new UserOrder();
        userOrder.setId("user-123");

        Order savedOrder = new Order();
        savedOrder.setId("order-123");
        savedOrder.setUser(userOrder);
        savedOrder.setCreationDate(System.currentTimeMillis());
        savedOrder.setDeliveryDir(dto.getDeliveryDir());

        when(userRestTemplate.existUser("user-123")).thenReturn(userOrder);
        when(productRestTemplate.productsAreOk(any())).thenReturn(listProductResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        ResponseEntity<?> response = orderService.createOrder(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(recurrentOrderRepository).save(any(RecurrentOrder.class));
    }


    private List<Order> getAuxOrders(){
        Order order1 = new Order();
        order1.setPrice(23.4);
        Order order2 = new Order();
        order2.setPrice(11.6);
        return List.of(order1,order2);
    }
}
