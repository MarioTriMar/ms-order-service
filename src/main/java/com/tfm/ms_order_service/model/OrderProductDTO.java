package com.tfm.ms_order_service.model;

import lombok.Data;

@Data
public class OrderProductDTO {
    private String productId;
    private int quantity;
}
