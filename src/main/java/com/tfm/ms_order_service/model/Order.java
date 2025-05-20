package com.tfm.ms_order_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(value = "orderEntity")
public class Order {

    @Id
    private String id;
    private double price;
    private String userId;
    private Long creationDate;
    private String deliveryDir;
    private List<OrderProduct> products;
    private String status;
}
