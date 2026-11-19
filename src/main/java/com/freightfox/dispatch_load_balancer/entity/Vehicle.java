package com.freightfox.dispatch_load_balancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
@Builder 
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "vehicle_id",
        nullable = false,
        unique = true,
        length = 50
    )
    private String vehicleId;

    @Column(
        nullable = false,
        precision = 12,
        scale = 3
    )
    private BigDecimal capacity;

    @Column(
        name = "current_latitude",
        nullable = false
    )
    private Double currentLatitude;

    @Column(
        name = "current_longitude",
        nullable = false
    )
    private Double currentLongitude;

    @Column(
        name = "current_address",
        nullable = false,
        length = 500
    )
    private String currentAddress;
}