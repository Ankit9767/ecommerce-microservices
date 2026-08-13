//package com.example.order_service.Client;
//
//
//import com.ecommerce.common.dto.ProductResponse;
//
//public interface ProductClient {
//
//    ProductResponse getProduct(Long productId);
//
//}

//was needed when using rest client , now using openFeign


package com.example.order_service.client;

import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.security.FeignSecurityConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(
//        name = "product-service",
//        url = "${service.product-service.url}"
//)
//use when not using the ureka server for service discovery

@FeignClient(
        name = "product-service",
        configuration = FeignSecurityConfiguration.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProduct(
            @PathVariable("id") Long productId
    );
}
