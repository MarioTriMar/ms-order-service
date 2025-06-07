package com.tfm.ms_order_service.service.restTemplate;

import com.tfm.ms_order_service.model.UserOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserRestTemplate {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.url.ms-user-service}")
    private String msUserServiceUrl;

    public UserOrder existUser(String id){
        String url = msUserServiceUrl + "/user/" + id + "/order";
        ResponseEntity<UserOrder> responseEntity = restTemplate.getForEntity(url, UserOrder.class);
        if(responseEntity.getStatusCode().is2xxSuccessful()){
            return responseEntity.getBody();
        }
        return null;
    }
}
