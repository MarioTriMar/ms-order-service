package com.tfm.ms_order_service.controller;

import com.tfm.ms_order_service.model.Order;
import com.tfm.ms_order_service.model.OrderDTO;
import com.tfm.ms_order_service.service.OrderService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping()
    public ResponseEntity createOrder(@RequestBody OrderDTO orderDto){
        if(!isValid(orderDto)){
            return new ResponseEntity<>("Invalid order", HttpStatus.BAD_REQUEST);
        }
        return orderService.createOrder(orderDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity getOrder(@PathVariable String id){
        if(id==null){
            return new ResponseEntity<>("Null id", HttpStatus.BAD_REQUEST);
        }
        return orderService.getOrder(id);
    }

    @GetMapping("/user")
    public ResponseEntity getOrdersOfUser(@RequestParam String user){
        if(user==null){
            return new ResponseEntity<>("Null user", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(orderService.getOrdersOfUser(user), HttpStatus.OK);
    }

    @GetMapping("/user/{id}/expended")
    public ResponseEntity getTotalExpendedByUser(@PathVariable String id){
        if (id==null){
            return new ResponseEntity<>("Null id", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(orderService.getTotalExpendedByUser(id), HttpStatus.OK);
    }

    private boolean isValid(OrderDTO order){
        if(order.getUserId() == null || order.getDeliveryDir() == null || order.getOrderProductDTO().isEmpty()){
            return false;
        }
        return true;
    }
}
