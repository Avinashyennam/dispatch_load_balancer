package com.freightfox.dispatch_load_balancer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", "Dispatch Load Balancer",
                "status", "running",
                "documentation", "/swagger-ui/index.html"
        );
    }
}