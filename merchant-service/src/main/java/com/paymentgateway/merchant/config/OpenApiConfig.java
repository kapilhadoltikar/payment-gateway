package com.paymentgateway.merchant.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Merchant Service API", version = "1.0", description = "API for merchant onboarding, management, and verification.", contact = @Contact(name = "Payment Gateway Support", email = "support@paymentgateway.com"), license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")), servers = @Server(url = "/api/v1"))
public class OpenApiConfig {
}
