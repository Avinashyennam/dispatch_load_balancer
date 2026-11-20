
package com.freightfox.dispatch_load_balancer.service;

import com.freightfox.dispatch_load_balancer.dto.VehicleRequest;
import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;
import com.freightfox.dispatch_load_balancer.entity.Vehicle;
import com.freightfox.dispatch_load_balancer.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public int upsertVehicles(List<VehicleRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one vehicle must be provided"
            );
        }

        // Reject duplicate IDs within the same request.
        Set<String> vehicleIds = new HashSet<>();

        for (VehicleRequest request : requests) {
            if (!vehicleIds.add(request.vehicleId())) {
                throw new IllegalArgumentException(
                        "Duplicate vehicle ID in request: "
                                + request.vehicleId()
                );
            }
        }

        // Fetch all existing vehicles in a single query.
        List<String> incomingIds = requests.stream()
                .map(VehicleRequest::vehicleId)
                .toList();

        Map<String, Vehicle> existingVehicles =
                vehicleRepository.findAllByVehicleIdIn(incomingIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Vehicle::getVehicleId,
                                Function.identity()
                        ));

        // Update existing entities or create new ones.
        List<Vehicle> vehiclesToSave = requests.stream()
                .map(request -> {
                    Vehicle vehicle = existingVehicles.get(
                            request.vehicleId()
                    );

                    if (vehicle == null) {
                        vehicle = new Vehicle();
                    }

                    mapToEntity(request, vehicle);

                    return vehicle;
                })
                .toList();

        return vehicleRepository.saveAll(vehiclesToSave).size();
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    private void mapToEntity(
            VehicleRequest request,
            Vehicle vehicle
    ) {
        vehicle.setVehicleId(request.vehicleId());
        vehicle.setCapacity(request.capacity());
        vehicle.setCurrentLatitude(request.currentLatitude());
        vehicle.setCurrentLongitude(request.currentLongitude());
        vehicle.setCurrentAddress(request.currentAddress());
    }
}
