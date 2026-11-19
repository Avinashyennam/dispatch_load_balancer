package com.freightfox.dispatch_load_balancer.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchPlanResponse {

    private List<VehicleDispatchResponse> dispatchPlan;
    private List<AssignedOrderResponse> unassignedOrders;
}