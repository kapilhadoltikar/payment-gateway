package com.paymentgateway.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.paymentgateway")
public class VaultServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VaultServiceApplication.class, args);
    }
}
