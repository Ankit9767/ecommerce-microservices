package com.example.product_service.service;

import com.ecommerce.common.events.ProductCreatedEvent;
import com.ecommerce.common.events.ProductDeletedEvent;
import com.ecommerce.common.events.ProductUpdatedEvent;

public interface OutboxService {

    void saveProductCreatedEvent(ProductCreatedEvent event);

    void saveProductUpdatedEvent(ProductUpdatedEvent event);

    void saveProductDeletedEvent(ProductDeletedEvent event);
}