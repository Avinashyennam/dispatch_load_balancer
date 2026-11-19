
package com.freightfox.dispatch_load_balancer.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record VehicleRequest(

        @NotBlank(message = "Vehicle ID is required")
        @Size(max = 50, message = "Vehicle ID cannot exceed 50 characters")
        String vehicleId,

        @NotNull(message = "Vehicle capacity is required")
        @DecimalMin(value = "0.001", message = "Vehicle capacity must be greater than zero")
        @Digits(integer = 9, fraction = 3, message = "Capacity supports up to 3 decimal places")
        BigDecimal capacity,

        @NotNull(message = "Current latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "Latitude must not exceed 90")
        Double currentLatitude,

        @NotNull(message = "Current longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "Longitude must not exceed 180")
        Double currentLongitude,

        @NotBlank(message = "Current address is required")
        @Size(max = 500, message = "Address cannot exceed 500 characters")
        String currentAddress

) {}
