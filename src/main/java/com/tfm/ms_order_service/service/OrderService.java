package com.tfm.ms_order_service.service;

import com.tfm.ms_order_service.model.*;
import com.tfm.ms_order_service.repository.OrderRepository;
import com.tfm.ms_order_service.service.restTemplate.ProductRestTemplate;
import com.tfm.ms_order_service.service.restTemplate.UserRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRestTemplate userRestTemplate;
    @Autowired
    private ProductRestTemplate productRestTemplate;

    public ResponseEntity createOrder(OrderDTO orderDto) {
        if(!userRestTemplate.existUser(orderDto.getUserId())){
            return new ResponseEntity<>("User doesnt exist", HttpStatus.BAD_REQUEST);
        }
        ListProductResponse listProductResponse = productRestTemplate.productsAreOk(orderDto.getOrderProductDTO());

        if(listProductResponse==null){
            return new ResponseEntity<>("Error in httpRequest", HttpStatus.BAD_GATEWAY);
        }
        List<String> productOutStocked = new ArrayList<>();
        for(ProductResponse productResponse: listProductResponse.getProductResponse()){
            if(!productResponse.isStockAllow())
                productOutStocked.add(productResponse.getName());
        }
        if(!productOutStocked.isEmpty()){
            return new ResponseEntity<>(productOutStocked.toString() + " without enough stock.", HttpStatus.BAD_REQUEST);
        }

        Order order = new Order();
        order.setUserId(orderDto.getUserId());
        order.setCreationDate(System.currentTimeMillis());
        order.setDeliveryDir(orderDto.getDeliveryDir());
        order.setStatus("ORDINARY");
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
        order.setPrice(total);

        order = orderRepository.save(order);

        return new ResponseEntity(order.getId()+ " created.", HttpStatus.CREATED);




    }

    public ResponseEntity getOrder(String id) {
        Optional<Order> optOrder = orderRepository.findById(id);
        if(optOrder.isEmpty()){
            return new ResponseEntity("Product not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(optOrder.get(),HttpStatus.OK);
    }

    public ResponseEntity getOrdersOfUser(String user) {
        List<Order> orders = orderRepository.findByUserId(user);
        if(orders.isEmpty()){
            return new ResponseEntity<>("No orders for user", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(orders, HttpStatus.OK);
    }
}
