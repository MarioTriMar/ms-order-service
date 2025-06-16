package com.tfm.ms_order_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(value = "recurrentOrderEntity")
public class RecurrentOrder {
    @Id
    private String id;
    private UserOrder user;
    private Long lastOrder;
    private String deliveryDir;
    private List<OrderProductDTO> products;
}
