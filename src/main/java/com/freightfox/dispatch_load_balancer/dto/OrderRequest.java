package com.freightfox.dispatch_load_balancer.dto;

import com.freightfox.dispatch_load_balancer.entity.enums.OrderPriority;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record OrderRequest(

        @NotBlank(message = "Order ID is required")
        @Size(max = 50, message = "Order ID cannot exceed 50 characters")
        String orderId,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "Latitude must not exceed 90")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "Longitude must not exceed 180")
        Double longitude,

        @NotBlank(message = "Address is required")
        @Size(max = 500, message = "Address cannot exceed 500 characters")
        String address,

        @NotNull(message = "Package weight is required")
        @DecimalMin(value = "0.001", message = "Package weight must be greater than zero")
        @Digits(integer = 9, fraction = 3, message = "Package weight supports up to 3 decimal places")
        BigDecimal packageWeight,

        @NotNull(message = "Priority is required")
        OrderPriority priority

) {}
