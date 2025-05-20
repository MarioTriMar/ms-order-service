package com.tfm.ms_order_service.model;

import lombok.Data;

@Data
public class ProductResponse {
    private String id;
    private String name;
    private String companyName;
    private double price;
    private int quantity;
    private boolean stockAllow;
}
