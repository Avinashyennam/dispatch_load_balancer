
package com.freightfox.dispatch_load_balancer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VehiclesRequest(

        @NotNull(message = "Vehicles list is required")
        @NotEmpty(message = "At least one vehicle must be provided")
        List<@Valid VehicleRequest> vehicles

) {}
