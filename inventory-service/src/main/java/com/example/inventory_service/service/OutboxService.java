package com.example.inventory_service.service;

import com.ecommerce.common.events.OutOfStockEvent;
import com.ecommerce.common.events.StockReleasedEvent;
import com.ecommerce.common.events.StockReservedEvent;
import com.ecommerce.common.events.StockUpdatedEvent;

public interface OutboxService {

    void saveStockReservedEvent(StockReservedEvent event);

    void saveStockReleasedEvent(StockReleasedEvent event);

    void saveStockUpdatedEvent(StockUpdatedEvent event);

    void saveOutOfStockEvent(OutOfStockEvent event);
}