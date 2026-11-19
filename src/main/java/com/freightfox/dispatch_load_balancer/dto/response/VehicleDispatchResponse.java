package com.freightfox.dispatch_load_balancer.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDispatchResponse {
    String vehicleId;
    BigDecimal totalLoad;
    String totalDistance;
    List<AssignedOrderResponse> assignedOrders;
}
