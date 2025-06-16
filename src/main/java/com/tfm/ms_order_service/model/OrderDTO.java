package com.tfm.ms_order_service.model;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private String userId;
    private List<OrderProductDTO> orderProductDTO;
    private String deliveryDir;
    private boolean makeRecurrent;
    private boolean recurrent;
}
