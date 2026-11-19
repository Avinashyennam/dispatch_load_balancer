package com.freightfox.dispatch_load_balancer.service.model;

import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;

import java.util.List;

public record DispatchAllocationResult(
        List<VehicleAllocation> vehicleAllocations,
        List<DeliveryOrder> unassignedOrders) {
    public DispatchAllocationResult {
        vehicleAllocations = List.copyOf(vehicleAllocations);
        unassignedOrders = List.copyOf(unassignedOrders);
    }
}
