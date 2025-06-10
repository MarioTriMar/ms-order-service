package com.tfm.ms_order_service.service.restTemplate;

import com.tfm.ms_order_service.model.ListProductResponse;
import com.tfm.ms_order_service.model.OrderProductDTO;
import com.tfm.ms_order_service.model.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class ProductRestTemplate {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${api.url.ms-product-service}")
    private String msProductServiceUrl;
    private Logger logger= LoggerFactory.getLogger(ProductRestTemplate.class);


    public ListProductResponse productsAreOk(List<OrderProductDTO> orderProductDTO){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<OrderProductDTO>> request = new HttpEntity<>(orderProductDTO, headers);
        String url = msProductServiceUrl + "/product/order";
        try {
            ResponseEntity<ListProductResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ListProductResponse.class
            );
            return response.getBody();
        }catch (HttpClientErrorException e){
            logger.error("HttpClientErrorException");
            return null;
        }catch (HttpServerErrorException e){
            logger.error("HttpServerErrorException");
            return null;
        }
    }
}
