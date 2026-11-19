
package com.freightfox.dispatch_load_balancer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrdersRequest(

        @NotNull(message = "Orders list is required")
        @NotEmpty(message = "At least one order must be provided")
        List<@Valid OrderRequest> orders

) {}
