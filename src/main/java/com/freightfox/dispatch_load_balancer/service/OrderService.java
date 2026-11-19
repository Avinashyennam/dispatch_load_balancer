
package com.freightfox.dispatch_load_balancer.service;

import com.freightfox.dispatch_load_balancer.dto.OrderRequest;
import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;
import com.freightfox.dispatch_load_balancer.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public int upsertOrders(List<OrderRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one order must be provided"
            );
        }

        // Reject duplicate IDs within the same request.
        Set<String> orderIds = new HashSet<>();

        for (OrderRequest request : requests) {
            if (!orderIds.add(request.orderId())) {
                throw new IllegalArgumentException(
                        "Duplicate order ID in request: " + request.orderId()
                );
            }
        }

        // Fetch all existing orders in a single query.
        List<String> incomingIds = requests.stream()
                .map(OrderRequest::orderId)
                .toList();

        Map<String, DeliveryOrder> existingOrders =
                orderRepository.findAllByOrderIdIn(incomingIds)
                        .stream()
                        .collect(Collectors.toMap(
                                DeliveryOrder::getOrderId,
                                Function.identity()
                        ));

        // Update existing entities or create new ones.
        List<DeliveryOrder> ordersToSave = requests.stream()
                .map(request -> {
                    DeliveryOrder order = existingOrders.get(
                            request.orderId()
                    );

                    if (order == null) {
                        order = new DeliveryOrder();
                    }

                    mapToEntity(request, order);

                    return order;
                })
                .toList();

        return orderRepository.saveAll(ordersToSave).size();
    }

    private void mapToEntity(
            OrderRequest request,
            DeliveryOrder order
    ) {
        order.setOrderId(request.orderId());
        order.setLatitude(request.latitude());
        order.setLongitude(request.longitude());
        order.setAddress(request.address());
        order.setPackageWeight(request.packageWeight());
        order.setPriority(request.priority());
    }
}
