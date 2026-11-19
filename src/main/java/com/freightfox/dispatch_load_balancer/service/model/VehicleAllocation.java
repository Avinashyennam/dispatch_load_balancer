package com.freightfox.dispatch_load_balancer.service.model;

import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;
import com.freightfox.dispatch_load_balancer.entity.Vehicle;

import java.util.List;

public record VehicleAllocation(
        Vehicle vehicle,
        List<DeliveryOrder> assignedOrders) {
    public VehicleAllocation {
        assignedOrders = List.copyOf(assignedOrders);
    }
}
