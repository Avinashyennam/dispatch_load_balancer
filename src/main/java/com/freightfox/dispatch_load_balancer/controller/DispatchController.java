
package com.freightfox.dispatch_load_balancer.controller;

import com.freightfox.dispatch_load_balancer.dto.OrdersRequest;
import com.freightfox.dispatch_load_balancer.dto.VehiclesRequest;
import com.freightfox.dispatch_load_balancer.dto.response.ApiResponse;
import com.freightfox.dispatch_load_balancer.service.DispatchPlanService;
import com.freightfox.dispatch_load_balancer.service.OrderService;
import com.freightfox.dispatch_load_balancer.service.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.freightfox.dispatch_load_balancer.dto.response.DispatchPlanResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dispatch Management", description = "Manage delivery orders, vehicles and dispatch plans")
@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

        private final OrderService orderService;
        private final VehicleService vehicleService;
        private final DispatchPlanService dispatchPlanService;

        @Operation(summary = "Add or update delivery orders")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Delivery orders accepted")
        @PostMapping("/orders")
        public ResponseEntity<ApiResponse> addOrders(
                        @Valid @RequestBody OrdersRequest request) {
                orderService.upsertOrders(request.orders());

                ApiResponse response = new ApiResponse(
                                "Delivery orders accepted.",
                                "success");

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(response);
        }


        @Operation(summary = "Add or update delivery vehicles")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicles accepted")
        @PostMapping("/vehicles")
        public ResponseEntity<ApiResponse> addVehicles(
                        @Valid @RequestBody VehiclesRequest request) {
                vehicleService.upsertVehicles(request.vehicles());

                ApiResponse response = new ApiResponse(
                                "Vehicle details accepted.",
                                "success");

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(response);
        }

        @GetMapping("/plan")
        public ResponseEntity<DispatchPlanResponse> getDispatchPlan() {
                DispatchPlanResponse response = dispatchPlanService.generateDispatchPlan();
                return new ResponseEntity<>(response, HttpStatus.OK);
        }

}
