package com.upi.customer;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CustomerConfig {

    @Bean
    @LoadBalanced // Enable load balancing between multiple instances of FRAUD service
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
