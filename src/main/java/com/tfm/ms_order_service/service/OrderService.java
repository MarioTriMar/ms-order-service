package com.tfm.ms_order_service.service;

import com.tfm.ms_order_service.model.*;
import com.tfm.ms_order_service.repository.OrderRepository;
import com.tfm.ms_order_service.repository.RecurrentOrderRepository;
import com.tfm.ms_order_service.service.restTemplate.ProductRestTemplate;
import com.tfm.ms_order_service.service.restTemplate.UserRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RecurrentOrderRepository recurrentOrderRepository;
    @Autowired
    private UserRestTemplate userRestTemplate;
    @Autowired
    private ProductRestTemplate productRestTemplate;


    @Autowired
    KafkaTemplate<String, Order> kafkaTemplate;



    public ResponseEntity createOrder(OrderDTO orderDto) {
        UserOrder userOrder = userRestTemplate.existUser(orderDto.getUserId());
        if(userOrder==null){
            return new ResponseEntity<>("User doesn't exist", HttpStatus.BAD_REQUEST);
        }
        ListProductResponse listProductResponse = productRestTemplate.productsAreOk(orderDto.getOrderProductDTO());

        if(listProductResponse==null){
            return new ResponseEntity<>("Error in httpRequest", HttpStatus.BAD_GATEWAY);
        }
        List<String> productOutStocked = new ArrayList<>();
        for(ProductResponse productResponse: listProductResponse.getProductResponse()){
            if(!productResponse.isStockAllow())
                productOutStocked.add(productResponse.getName());
                log.info("Product without enough stock: [{}]",productResponse.getName());
        }
        if(!productOutStocked.isEmpty()){
            return new ResponseEntity<>(productOutStocked.toString() + " without enough stock.", HttpStatus.BAD_REQUEST);
        }
        log.info("All products have enough stock");
        Order order = new Order();
        order.setUser(userOrder);
        order.setCreationDate(System.currentTimeMillis());
        order.setDeliveryDir(orderDto.getDeliveryDir());
        order.setStatus(orderDto.isRecurrent() || orderDto.isMakeRecurrent() ? "RECURRENT" : "ORDINARY");
        List<OrderProduct> products = new ArrayList<>();
        double total = 0;
        for(ProductResponse productResponse: listProductResponse.getProductResponse()){
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setId(productResponse.getId());
            orderProduct.setName(productResponse.getName());
            orderProduct.setCompanyName(productResponse.getCompanyName());
            orderProduct.setPrice(productResponse.getPrice());
            orderProduct.setQuantity(productResponse.getQuantity());
            products.add(orderProduct);
            total += orderProduct.getPrice() * orderProduct.getQuantity();
        }
        order.setProducts(products);
        order.setPrice(Math.floor(total*100)/100.0);
        order = orderRepository.save(order);
        log.info("Order created: [{}]", order.toString());
        kafkaTemplate.send("email-service-topic", order.getId(), order);
        log.info("Event sent to email-service-topic");
        if(orderDto.isMakeRecurrent()){
            RecurrentOrder recurrentOrder = new RecurrentOrder();
            recurrentOrder.setUser(order.getUser());
            recurrentOrder.setLastOrder(order.getCreationDate());
            recurrentOrder.setDeliveryDir(order.getDeliveryDir());
            recurrentOrder.setProducts(orderDto.getOrderProductDTO());
            recurrentOrderRepository.save(recurrentOrder);
            log.info("RecurrentOrder created");
        }
        return new ResponseEntity(order.getId()+ " created.", HttpStatus.CREATED);

    }

    public ResponseEntity getOrder(String id) {
        Optional<Order> optOrder = orderRepository.findById(id);
        if(optOrder.isEmpty()){
            return new ResponseEntity("Product not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(optOrder.get(),HttpStatus.OK);
    }

    public List<Order> getOrdersOfUser(String user) {
        List<Order> orders = orderRepository.findByUserId(user);
        return orders;
    }

    public double getTotalExpendedByUser(String id) {
        List<Order> orders = getOrdersOfUser(id);
        double total = 0;
        if(orders.isEmpty()){
            return total;
        }
        for(Order order: orders){
            total+=order.getPrice();
        }
        return total;

    }
}
