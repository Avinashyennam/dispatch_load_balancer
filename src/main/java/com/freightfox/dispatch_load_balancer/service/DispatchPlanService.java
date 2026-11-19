package com.freightfox.dispatch_load_balancer.service;

import com.freightfox.dispatch_load_balancer.dto.response.AssignedOrderResponse;
import com.freightfox.dispatch_load_balancer.dto.response.DispatchPlanResponse;
import com.freightfox.dispatch_load_balancer.dto.response.VehicleDispatchResponse;
import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;
import com.freightfox.dispatch_load_balancer.entity.Vehicle;
import com.freightfox.dispatch_load_balancer.repository.OrderRepository;
import com.freightfox.dispatch_load_balancer.repository.VehicleRepository;
import com.freightfox.dispatch_load_balancer.service.model.DispatchAllocationResult;
import com.freightfox.dispatch_load_balancer.service.model.VehicleAllocation;
import com.freightfox.dispatch_load_balancer.util.HaversineDistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DispatchPlanService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchAllocationService allocationService;

    @Transactional(readOnly = true)
    public DispatchPlanResponse generateDispatchPlan() {

        // Fetch all orders and vehicles from the database.
        List<DeliveryOrder> orders = orderRepository.findAll();
        List<Vehicle> vehicles = vehicleRepository.findAll();

        // Allocate orders based on priority, capacity and route cost.
        DispatchAllocationResult allocationResult = allocationService.allocate(vehicles, orders);

        // Convert internal allocation results into API response DTOs.
        List<VehicleDispatchResponse> vehicleResponses = allocationResult.vehicleAllocations()
                .stream()
                .map(this::buildVehicleResponse)
                .toList();

        List<AssignedOrderResponse> unassignedOrders = allocationResult.unassignedOrders()
                .stream()
                .map(this::mapOrder)
                .toList();

        return new DispatchPlanResponse(
                vehicleResponses,
                unassignedOrders);
    }

    private VehicleDispatchResponse buildVehicleResponse(
            VehicleAllocation allocation) {

        Vehicle vehicle = allocation.vehicle();
        List<DeliveryOrder> orders = allocation.assignedOrders();

        BigDecimal totalLoad = orders.stream()
                .map(DeliveryOrder::getPackageWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalDistance = calculateTotalDistance(vehicle, orders);

        List<AssignedOrderResponse> assignedOrderResponses = orders.stream()
                .map(this::mapOrder)
                .toList();

        return VehicleDispatchResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .totalLoad(totalLoad)
                .totalDistance(formatDistance(totalDistance))
                .assignedOrders(assignedOrderResponses)
                .build();
    }

    private double calculateTotalDistance(
            Vehicle vehicle,
            List<DeliveryOrder> orders) {

        double totalDistance = 0.0;

        double previousLatitude = vehicle.getCurrentLatitude();
        double previousLongitude = vehicle.getCurrentLongitude();

        for (DeliveryOrder order : orders) {

            totalDistance += HaversineDistanceCalculator.calculateDistance(
                    previousLatitude,
                    previousLongitude,
                    order.getLatitude(),
                    order.getLongitude());

            previousLatitude = order.getLatitude();
            previousLongitude = order.getLongitude();
        }

        return totalDistance;
    }

    private String formatDistance(double distance) {
        return String.format(
                Locale.ROOT,
                "%.2f km",
                distance);
    }

    private AssignedOrderResponse mapOrder(DeliveryOrder order) {
        return AssignedOrderResponse.builder()
                .orderId(order.getOrderId())
                .latitude(order.getLatitude())
                .longitude(order.getLongitude())
                .address(order.getAddress())
                .packageWeight(order.getPackageWeight())
                .priority(order.getPriority())
                .build();
    }
}
