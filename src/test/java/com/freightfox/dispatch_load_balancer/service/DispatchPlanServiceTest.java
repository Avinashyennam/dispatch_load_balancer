
package com.freightfox.dispatch_load_balancer.service;

import com.freightfox.dispatch_load_balancer.dto.response.DispatchPlanResponse;
import com.freightfox.dispatch_load_balancer.dto.response.VehicleDispatchResponse;
import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;
import com.freightfox.dispatch_load_balancer.entity.Vehicle;
import com.freightfox.dispatch_load_balancer.entity.enums.OrderPriority;
import com.freightfox.dispatch_load_balancer.repository.OrderRepository;
import com.freightfox.dispatch_load_balancer.repository.VehicleRepository;
import com.freightfox.dispatch_load_balancer.service.model.DispatchAllocationResult;
import com.freightfox.dispatch_load_balancer.service.model.VehicleAllocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchPlanServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DispatchAllocationService allocationService;

    @InjectMocks
    private DispatchPlanService dispatchPlanService;


    @Test
    void generateDispatchPlan_shouldReturnVehicleDetailsAndRouteDistance() {

        Vehicle vehicle = createVehicle(
                "V001", "10.000",
                0.0, 0.0
        );

        DeliveryOrder order1 = createOrder(
                "ORD001", "2.500",
                0.0, 1.0,
                OrderPriority.HIGH
        );

        DeliveryOrder order2 = createOrder(
                "ORD002", "3.500",
                0.0, 2.0,
                OrderPriority.MEDIUM
        );

        List<Vehicle> vehicles = List.of(vehicle);
        List<DeliveryOrder> orders = List.of(order1, order2);

        VehicleAllocation vehicleAllocation =
                new VehicleAllocation(vehicle, List.of(order1, order2));

        DispatchAllocationResult allocationResult =
                new DispatchAllocationResult(
                        List.of(vehicleAllocation),
                        List.of()
                );

        when(orderRepository.findAll()).thenReturn(orders);
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(allocationService.allocate(vehicles, orders))
                .thenReturn(allocationResult);

        DispatchPlanResponse response =
                dispatchPlanService.generateDispatchPlan();

        assertNotNull(response);
        assertEquals(1, response.getDispatchPlan().size());
        assertTrue(response.getUnassignedOrders().isEmpty());

        VehicleDispatchResponse vehicleResponse =
                response.getDispatchPlan().get(0);

        assertEquals("V001", vehicleResponse.getVehicleId());
        assertEquals(new BigDecimal("6.000"), vehicleResponse.getTotalLoad());
        assertEquals("222.39 km", vehicleResponse.getTotalDistance());

        assertEquals(2, vehicleResponse.getAssignedOrders().size());
        assertEquals("ORD001",
                vehicleResponse.getAssignedOrders().get(0).getOrderId());
        assertEquals("ORD002",
                vehicleResponse.getAssignedOrders().get(1).getOrderId());

        verify(allocationService).allocate(vehicles, orders);
    }


    @Test
    void generateDispatchPlan_shouldIncludeUnassignedOrders() {

        Vehicle vehicle = createVehicle(
                "V001", "5.000",
                0.0, 0.0
        );

        DeliveryOrder assignedOrder = createOrder(
                "ORD001", "3.000",
                0.0, 1.0,
                OrderPriority.HIGH
        );

        DeliveryOrder unassignedOrder = createOrder(
                "ORD002", "8.000",
                0.0, 2.0,
                OrderPriority.LOW
        );

        List<Vehicle> vehicles = List.of(vehicle);
        List<DeliveryOrder> orders =
                List.of(assignedOrder, unassignedOrder);

        DispatchAllocationResult allocationResult =
                new DispatchAllocationResult(
                        List.of(new VehicleAllocation(
                                vehicle,
                                List.of(assignedOrder)
                        )),
                        List.of(unassignedOrder)
                );

        when(orderRepository.findAll()).thenReturn(orders);
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(allocationService.allocate(vehicles, orders))
                .thenReturn(allocationResult);

        DispatchPlanResponse response =
                dispatchPlanService.generateDispatchPlan();

        assertEquals(1, response.getDispatchPlan().size());
        assertEquals(1, response.getUnassignedOrders().size());

        assertEquals(
                "ORD002",
                response.getUnassignedOrders().get(0).getOrderId()
        );

        assertEquals(
                new BigDecimal("8.000"),
                response.getUnassignedOrders().get(0).getPackageWeight()
        );
    }


    @Test
    void generateDispatchPlan_shouldReturnEmptyPlanWhenNoDataExists() {

        List<Vehicle> vehicles = List.of();
        List<DeliveryOrder> orders = List.of();

        DispatchAllocationResult allocationResult =
                new DispatchAllocationResult(
                        List.of(),
                        List.of()
                );

        when(orderRepository.findAll()).thenReturn(orders);
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(allocationService.allocate(vehicles, orders))
                .thenReturn(allocationResult);

        DispatchPlanResponse response =
                dispatchPlanService.generateDispatchPlan();

        assertNotNull(response);
        assertNotNull(response.getDispatchPlan());
        assertNotNull(response.getUnassignedOrders());

        assertTrue(response.getDispatchPlan().isEmpty());
        assertTrue(response.getUnassignedOrders().isEmpty());

        verify(allocationService).allocate(vehicles, orders);
    }


    @Test
    void generateDispatchPlan_shouldHandleVehicleWithoutAssignedOrders() {

        Vehicle vehicle = createVehicle(
                "V001", "10.000",
                28.6139, 77.2090
        );

        List<Vehicle> vehicles = List.of(vehicle);
        List<DeliveryOrder> orders = List.of();

        DispatchAllocationResult allocationResult =
                new DispatchAllocationResult(
                        List.of(new VehicleAllocation(
                                vehicle,
                                List.of()
                        )),
                        List.of()
                );

        when(orderRepository.findAll()).thenReturn(orders);
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(allocationService.allocate(vehicles, orders))
                .thenReturn(allocationResult);

        DispatchPlanResponse response =
                dispatchPlanService.generateDispatchPlan();

        assertEquals(1, response.getDispatchPlan().size());

        VehicleDispatchResponse vehicleResponse =
                response.getDispatchPlan().get(0);

        assertEquals("V001", vehicleResponse.getVehicleId());
        assertEquals(BigDecimal.ZERO, vehicleResponse.getTotalLoad());
        assertEquals("0.00 km", vehicleResponse.getTotalDistance());
        assertTrue(vehicleResponse.getAssignedOrders().isEmpty());
    }


    private Vehicle createVehicle(
            String vehicleId,
            String capacity,
            Double latitude,
            Double longitude) {

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(vehicleId);
        vehicle.setCapacity(new BigDecimal(capacity));
        vehicle.setCurrentLatitude(latitude);
        vehicle.setCurrentLongitude(longitude);
        vehicle.setCurrentAddress("Delhi");

        return vehicle;
    }


    private DeliveryOrder createOrder(
            String orderId,
            String weight,
            Double latitude,
            Double longitude,
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
