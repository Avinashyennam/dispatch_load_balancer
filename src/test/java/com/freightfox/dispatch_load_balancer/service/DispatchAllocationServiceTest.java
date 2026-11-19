package com.freightfox.dispatch_load_balancer.service;

import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;
import com.freightfox.dispatch_load_balancer.entity.Vehicle;
import com.freightfox.dispatch_load_balancer.entity.enums.OrderPriority;
import com.freightfox.dispatch_load_balancer.service.model.DispatchAllocationResult;
import com.freightfox.dispatch_load_balancer.service.model.VehicleAllocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DispatchAllocationServiceTest {

    private DispatchAllocationService allocationService;

    @BeforeEach
    void setUp() {
        allocationService = new DispatchAllocationService();
    }

    @Test
    void shouldAssignOrderToAvailableVehicle() {

        Vehicle vehicle = createVehicle(
                "V1", "100", 28.6139, 77.2090
        );

        DeliveryOrder order = createOrder(
                "O1", "20", 28.5355, 77.3910,
                OrderPriority.HIGH
        );

        DispatchAllocationResult result =
                allocationService.allocate(
                        List.of(vehicle),
                        List.of(order)
                );

        assertEquals(1, result.vehicleAllocations().size());

        VehicleAllocation allocation = result.vehicleAllocations().get(0);

        assertEquals("V1", allocation.vehicle().getVehicleId());
        assertEquals(1, allocation.assignedOrders().size());
        assertEquals("O1",
                allocation.assignedOrders().get(0).getOrderId());

        assertTrue(result.unassignedOrders().isEmpty());
    }

    @Test
    void shouldSelectVehicleWithMinimumAdditionalDistance() {

        Vehicle vehicle1 = createVehicle(
                "V1", "100", 0.0, 0.0
        );

        Vehicle vehicle2 = createVehicle(
                "V2", "100", 0.0, 10.0
        );

        DeliveryOrder order = createOrder(
                "O1", "10", 0.0, 9.0,
                OrderPriority.HIGH
        );

        DispatchAllocationResult result =
                allocationService.allocate(
                        List.of(vehicle1, vehicle2),
                        List.of(order)
                );

        VehicleAllocation selectedAllocation =
                result.vehicleAllocations().stream()
                        .filter(a -> !a.assignedOrders().isEmpty())
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                "V2",
                selectedAllocation.vehicle().getVehicleId()
        );
    }

    @Test
    void shouldNotExceedVehicleCapacity() {

        Vehicle vehicle = createVehicle(
                "V1", "50", 28.6139, 77.2090
        );

        DeliveryOrder order1 = createOrder(
                "O1", "30", 28.5355, 77.3910,
                OrderPriority.HIGH
        );

        DeliveryOrder order2 = createOrder(
                "O2", "25", 28.4595, 77.0266,
                OrderPriority.MEDIUM
        );

        DispatchAllocationResult result =
                allocationService.allocate(
                        List.of(vehicle),
                        List.of(order1, order2)
                );

        VehicleAllocation allocation =
                result.vehicleAllocations().get(0);

        assertEquals(1, allocation.assignedOrders().size());
        assertEquals("O1",
                allocation.assignedOrders().get(0).getOrderId());

        assertEquals(1, result.unassignedOrders().size());
        assertEquals("O2",
                result.unassignedOrders().get(0).getOrderId());
    }

    @Test
    void shouldAssignHighPriorityOrderBeforeLowPriorityOrder() {

        Vehicle vehicle = createVehicle(
                "V1", "10", 28.6139, 77.2090
        );

        DeliveryOrder lowPriorityOrder = createOrder(
                "O1", "10", 28.5355, 77.3910,
                OrderPriority.LOW
        );

        DeliveryOrder highPriorityOrder = createOrder(
                "O2", "10", 28.4595, 77.0266,
                OrderPriority.HIGH
        );

        DispatchAllocationResult result =
                allocationService.allocate(
                        List.of(vehicle),
                        List.of(lowPriorityOrder, highPriorityOrder)
                );

        VehicleAllocation allocation =
                result.vehicleAllocations().get(0);

        assertEquals(1, allocation.assignedOrders().size());

        assertEquals(
                "O2",
                allocation.assignedOrders().get(0).getOrderId()
        );

        assertEquals(1, result.unassignedOrders().size());
        assertEquals(
                "O1",
                result.unassignedOrders().get(0).getOrderId()
        );
    }

    @Test
    void shouldInsertOrderAtCheapestRoutePosition() {

        Vehicle vehicle = createVehicle(
                "V1", "100", 0.0, 0.0
        );

        // O1 is processed first because its ID comes first.
        DeliveryOrder order1 = createOrder(
                "O1", "10", 0.0, 10.0,
                OrderPriority.HIGH
        );

        // O2 lies between the vehicle and O1.
        DeliveryOrder order2 = createOrder(
                "O2", "10", 0.0, 5.0,
                OrderPriority.HIGH
        );

        DispatchAllocationResult result =
                allocationService.allocate(
                        List.of(vehicle),
                        List.of(order1, order2)
                );

        List<DeliveryOrder> assignedOrders =
                result.vehicleAllocations().get(0).assignedOrders();

        assertEquals(2, assignedOrders.size());

        assertEquals("O2", assignedOrders.get(0).getOrderId());
        assertEquals("O1", assignedOrders.get(1).getOrderId());
    }

    @Test
    void shouldMarkAllOrdersUnassignedWhenNoVehiclesExist() {

        DeliveryOrder order = createOrder(
                "O1", "10", 28.6139, 77.2090,
                OrderPriority.HIGH
        );

        DispatchAllocationResult result =
                allocationService.allocate(
                        List.of(),
                        List.of(order)
                );

        assertTrue(result.vehicleAllocations().isEmpty());
        assertEquals(1, result.unassignedOrders().size());
    }

    @Test
    void shouldReturnEmptyAllocationsWhenNoOrdersExist() {

        Vehicle vehicle = createVehicle(
                "V1", "100", 28.6139, 77.2090
        );

        DispatchAllocationResult result =
                allocationService.allocate(
                        List.of(vehicle),
                        List.of()
                );

        assertEquals(1, result.vehicleAllocations().size());
        assertTrue(
                result.vehicleAllocations().get(0)
                        .assignedOrders().isEmpty()
        );
        assertTrue(result.unassignedOrders().isEmpty());
    }

    @Test
    void shouldRejectNullVehiclesOrOrders() {

        assertThrows(
                IllegalArgumentException.class,
                () -> allocationService.allocate(null, List.of())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> allocationService.allocate(List.of(), null)
        );
    }

    // ---------------- Helper methods ----------------

    private Vehicle createVehicle(
            String vehicleId,
            String capacity,
            double latitude,
            double longitude) {

        Vehicle vehicle = new Vehicle();

        vehicle.setVehicleId(vehicleId);
        vehicle.setCapacity(new BigDecimal(capacity));
        vehicle.setCurrentLatitude(latitude);
        vehicle.setCurrentLongitude(longitude);
        vehicle.setCurrentAddress("Vehicle starting location");

        return vehicle;
    }

    private DeliveryOrder createOrder(
            String orderId,
            String weight,
            double latitude,
            double longitude,
            OrderPriority priority) {

        DeliveryOrder order = new DeliveryOrder();

        order.setOrderId(orderId);
        order.setPackageWeight(new BigDecimal(weight));
        order.setLatitude(latitude);
        order.setLongitude(longitude);
        order.setAddress("Delivery address " + orderId);
        order.setPriority(priority);

        return order;
    }
}
