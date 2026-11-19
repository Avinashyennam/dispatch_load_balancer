
package com.freightfox.dispatch_load_balancer.service;

import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;
import com.freightfox.dispatch_load_balancer.entity.Vehicle;
import com.freightfox.dispatch_load_balancer.entity.enums.OrderPriority;
import com.freightfox.dispatch_load_balancer.service.model.DispatchAllocationResult;
import com.freightfox.dispatch_load_balancer.service.model.VehicleAllocation;
import com.freightfox.dispatch_load_balancer.util.HaversineDistanceCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DispatchAllocationService {

    public DispatchAllocationResult allocate(
            List<Vehicle> vehicles,
            List<DeliveryOrder> orders) {

        if (vehicles == null || orders == null) {
            throw new IllegalArgumentException(
                    "Vehicles and orders must not be null."
            );
        }

        // Sort vehicles to ensure deterministic tie-breaking.
        List<Vehicle> sortedVehicles = vehicles.stream()
                .sorted(Comparator.comparing(Vehicle::getVehicleId))
                .toList();

        // Allocate HIGH priority orders before MEDIUM and LOW.
        List<DeliveryOrder> sortedOrders = orders.stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        (DeliveryOrder order) ->
                                                getPriorityRank(order.getPriority())
                                )
                                .thenComparing(DeliveryOrder::getOrderId)
                )
                .toList();

        Map<Vehicle, List<DeliveryOrder>> assignedOrders = new HashMap<>();

        Map<Vehicle, BigDecimal> currentLoads = new HashMap<>();

        for (Vehicle vehicle : sortedVehicles) {
            assignedOrders.put(vehicle, new ArrayList<>());
            currentLoads.put(vehicle, BigDecimal.ZERO);
        }

        List<DeliveryOrder> unassignedOrders = new ArrayList<>();

        for (DeliveryOrder order : sortedOrders) {

            Vehicle bestVehicle = null;
            int bestInsertionIndex = -1;
            double minimumAdditionalDistance = Double.POSITIVE_INFINITY;

            for (Vehicle vehicle : sortedVehicles) {

                BigDecimal currentLoad = currentLoads.get(vehicle);

                BigDecimal resultingLoad =
                        currentLoad.add(order.getPackageWeight());

                // Never exceed vehicle capacity.
                if (resultingLoad.compareTo(vehicle.getCapacity()) > 0) {
                    continue;
                }

                List<DeliveryOrder> route = assignedOrders.get(vehicle);

                // Try inserting the order at every possible position.
                for (int index = 0; index <= route.size(); index++) {

                    double additionalDistance =
                            calculateInsertionCost(vehicle, route, order, index);

                    if (isBetterCandidate(
                            additionalDistance,
                            vehicle,
                            index,
                            minimumAdditionalDistance,
                            bestVehicle,
                            bestInsertionIndex)) {

                        minimumAdditionalDistance = additionalDistance;
                        bestVehicle = vehicle;
                        bestInsertionIndex = index;
                    }
                }
            }

            if (bestVehicle == null) {
                unassignedOrders.add(order);
                continue;
            }

            // Insert the order at the position with minimum added distance.
            assignedOrders.get(bestVehicle).add(bestInsertionIndex, order);

            currentLoads.put(
                    bestVehicle,
                    currentLoads.get(bestVehicle).add(order.getPackageWeight())
            );
        }

        List<VehicleAllocation> allocations = sortedVehicles.stream()
                .map(vehicle -> new VehicleAllocation(
                        vehicle,
                        assignedOrders.get(vehicle)
                ))
                .toList();

        return new DispatchAllocationResult(
                allocations,
                unassignedOrders
        );
    }

    private double calculateInsertionCost(
            Vehicle vehicle,
            List<DeliveryOrder> route,
            DeliveryOrder newOrder,
            int insertionIndex) {

        double previousLatitude;
        double previousLongitude;

        // Previous location is either the vehicle's starting point
        // or the preceding delivery order.
        if (insertionIndex == 0) {
            previousLatitude = vehicle.getCurrentLatitude();
            previousLongitude = vehicle.getCurrentLongitude();
        } else {
            DeliveryOrder previousOrder = route.get(insertionIndex - 1);
            previousLatitude = previousOrder.getLatitude();
            previousLongitude = previousOrder.getLongitude();
        }

        double distanceToNewOrder =
                distance(
                        previousLatitude,
                        previousLongitude,
                        newOrder.getLatitude(),
                        newOrder.getLongitude()
                );

        // Insertion at the end of the route.
        if (insertionIndex == route.size()) {
            return distanceToNewOrder;
        }

        // If an order already exists after the insertion position,
        // calculate the distance saved by replacing the old direct edge.
        DeliveryOrder nextOrder = route.get(insertionIndex);

        double oldDistance = distance(
                previousLatitude,
                previousLongitude,
                nextOrder.getLatitude(),
                nextOrder.getLongitude()
        );

        double newDistance = distance(
                newOrder.getLatitude(),
                newOrder.getLongitude(),
                nextOrder.getLatitude(),
                nextOrder.getLongitude()
        );

        return Math.max(
                0.0,
                distanceToNewOrder + newDistance - oldDistance
        );
    }

    private double distance(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2) {

        return HaversineDistanceCalculator.calculateDistance(
                latitude1,
                longitude1,
                latitude2,
                longitude2
        );
    }

    private boolean isBetterCandidate(
            double candidateDistance,
            Vehicle candidateVehicle,
            int candidateIndex,
            double currentBestDistance,
            Vehicle currentBestVehicle,
            int currentBestIndex) {

        int distanceComparison =
                Double.compare(candidateDistance, currentBestDistance);

        if (distanceComparison < 0) {
            return true;
        }

        if (distanceComparison > 0) {
            return false;
        }

        // Deterministic tie-breaking by vehicle ID.
        if (currentBestVehicle == null) {
            return true;
        }

        int vehicleComparison = candidateVehicle.getVehicleId()
                .compareTo(currentBestVehicle.getVehicleId());

        if (vehicleComparison != 0) {
            return vehicleComparison < 0;
        }

        return candidateIndex < currentBestIndex;
    }

    private int getPriorityRank(OrderPriority priority) {
        return switch (priority) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }
}
