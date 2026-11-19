
package com.freightfox.dispatch_load_balancer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dispatchLoadBalancerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dispatch Load Balancer API")
                        .description("""
                                REST APIs for managing delivery orders and vehicles
                                and generating optimized dispatch plans based on
                                vehicle capacity, order priority and Haversine distance.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FreightFox Assignment")));
    }
}
