package com.tfm.ms_order_service.model;

import lombok.Data;

@Data
public class OrderProduct {
    private String id;
    private String name;
    private String companyName;
    private double price;
    private int quantity;
}
