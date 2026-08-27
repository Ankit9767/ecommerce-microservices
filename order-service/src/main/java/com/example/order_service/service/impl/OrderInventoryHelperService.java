package com.example.order_service.service.impl;

import com.ecommerce.common.dto.InventoryQuantityRequest;
import com.ecommerce.common.exception.InventoryConfirmException;
import com.ecommerce.common.exception.InventoryReleaseException;
import com.ecommerce.common.exception.InventoryReservationException;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.metrics.OrderMetrics;
import com.example.order_service.client.InventoryClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderInventoryHelperService {

    private final InventoryClient inventoryClient;

    private final OrderMetrics orderMetrics;

    public OrderInventoryHelperService(InventoryClient inventoryClient,
                                       OrderMetrics orderMetrics) {

        this.inventoryClient = inventoryClient;
        this.orderMetrics = orderMetrics;
    }

    /**
     * Reserve inventory for a newly created order.
     */
    public void reserveInventory(Order order) {

        if (order.getReservationId() == null) {

            throw new InventoryReservationException(
                    "Order reservationId must not be null"
            );
        }

        List<OrderItem> reservedItems = new ArrayList<>();

        try {

            for (OrderItem item : order.getItems()) {

                inventoryClient.reserveStock(
                        item.getProductId(),
                        new InventoryQuantityRequest(
                                item.getQuantity(),
                                order.getReservationId()
                        )
                );

                reservedItems.add(item);
            }

        } catch (RuntimeException ex) {

            orderMetrics.inventoryReservationFailed();

            // Compensate for reservations already made.
            releaseInventory(reservedItems, order.getReservationId());

            throw new InventoryReservationException(
                    "Unable to reserve inventory for order"
            );
        }
    }

    /**
     * Release all inventory associated with the given order items.
     */
    public void releaseInventory(List<OrderItem> items,
                                 UUID reservationId) {

        for (OrderItem item : items) {

            try {

                inventoryClient.releaseStock(
                        item.getProductId(),
                        new InventoryQuantityRequest(
                                item.getQuantity(),
                                reservationId
                        )
                );

            } catch (RuntimeException ex) {

                orderMetrics.inventoryCompensationFailed();
            }
        }
    }

    /**
     * During an order update, reserve only the additional
     * inventory required by the new order.
     */
    public List<InventoryAdjustment> reserveInventoryForUpdate(List<OrderItem> existingItems,
                                                               List<OrderItem> newItems,
                                                               UUID reservationId) {

        Map<Long, Integer> existingQuantities =
                existingItems.stream()
                        .collect(
                                Collectors.toMap(
                                        OrderItem::getProductId,
                                        OrderItem::getQuantity
                                )
                        );

        Map<Long, Integer> newQuantities =
                newItems.stream()
                        .collect(
                                Collectors.toMap(
                                        OrderItem::getProductId,
                                        OrderItem::getQuantity
                                )
                        );

        List<InventoryAdjustment> reservations = new ArrayList<>();

        try {

            for (Map.Entry<Long, Integer> entry :
                    newQuantities.entrySet()) {

                Long productId = entry.getKey();

                int newQuantity = entry.getValue();

                int oldQuantity =
                        existingQuantities.getOrDefault(
                                productId,
                                0
                        );

                int additionalQuantity = newQuantity - oldQuantity;

                if (additionalQuantity <= 0) {
                    continue;
                }

                inventoryClient.reserveStock(
                        productId,
                        new InventoryQuantityRequest(
                                additionalQuantity,
                                reservationId
                        )
                );

                reservations.add(
                        new InventoryAdjustment(
                                productId,
                                additionalQuantity,
                                reservationId
                        )
                );
            }

            return reservations;

        } catch (RuntimeException ex) {

            releaseInventoryAdjustments(reservations);

            orderMetrics.inventoryReservationFailed();

            throw new InventoryReservationException(
                    "Unable to reserve additional inventory for order"
            );
        }
    }

    /**
     * Release inventory that is no longer required
     * after an order update.
     */
    public void releaseReducedInventory(List<OrderItem> existingItems,
                                        List<OrderItem> newItems,
                                        UUID reservationId) {

        Map<Long, Integer> existingQuantities =
                existingItems.stream()
                        .collect(
                                Collectors.toMap(
                                        OrderItem::getProductId,
                                        OrderItem::getQuantity
                                )
                        );

        Map<Long, Integer> newQuantities =
                newItems.stream()
                        .collect(
                                Collectors.toMap(
                                        OrderItem::getProductId,
                                        OrderItem::getQuantity
                                )
                        );

        for (Map.Entry<Long, Integer> entry :
                existingQuantities.entrySet()) {

            Long productId = entry.getKey();

            int oldQuantity = entry.getValue();

            int newQuantity =
                    newQuantities.getOrDefault(
                            productId,
                            0
                    );

            int releasedQuantity = oldQuantity - newQuantity;

            if (releasedQuantity <= 0) {
                continue;
            }

            try {

                inventoryClient.releaseStock(
                        productId,
                        new InventoryQuantityRequest(
                                releasedQuantity,
                                reservationId
                        )
                );

            } catch (RuntimeException ex) {

                orderMetrics.inventoryCompensationFailed();

                throw new InventoryReleaseException(
                        productId,
                        releasedQuantity
                );
            }
        }
    }

    /**
     * Confirm (commit) the reservations of an order that has been paid.
     */
    public void confirmReservations(Order order) {

        UUID reservationId = order.getReservationId();

        for (OrderItem item : order.getItems()) {

            try {

                inventoryClient.confirmReservation(
                        item.getProductId(),
                        new InventoryQuantityRequest(
                                item.getQuantity(),
                                reservationId
                        )
                );

            } catch (RuntimeException ex) {

                throw new InventoryConfirmException(
                        item.getProductId(),
                        item.getQuantity()
                );
            }
        }
    }

    /**
     * Restore inventory reservations made during
     * an order update when persistence fails.
     */
    public void releaseInventoryAdjustments(List<InventoryAdjustment> adjustments) {

        for (InventoryAdjustment adjustment : adjustments) {

            try {

                inventoryClient.releaseStock(
                        adjustment.productId(),
                        new InventoryQuantityRequest(
                                adjustment.quantity(),
                                adjustment.reservationId()
                        )
                );

            } catch (RuntimeException ex) {

                orderMetrics.inventoryCompensationFailed();
            }
        }
    }

    public record InventoryAdjustment(
            Long productId,
            Integer quantity,
            UUID reservationId
    ) {
    }
}