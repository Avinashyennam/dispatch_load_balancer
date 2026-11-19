package com.freightfox.dispatch_load_balancer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freightfox.dispatch_load_balancer.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByVehicleId(String vehicleId);
    List<Vehicle> findAllByVehicleIdIn(List<String> vehicleIds);
    boolean existsByVehicleId(String vehicleId);
}
