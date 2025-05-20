package com.tfm.ms_order_service.service.restTemplate;

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

    public boolean existUser(String id){
        String url = msUserServiceUrl + "/user/" + id;
        ResponseEntity<Boolean> responseEntity = restTemplate.getForEntity(url, Boolean.class);
        if(responseEntity.getStatusCode().is2xxSuccessful() &&
            responseEntity.getBody()){
            return true;
        }
        return false;
    }
}
