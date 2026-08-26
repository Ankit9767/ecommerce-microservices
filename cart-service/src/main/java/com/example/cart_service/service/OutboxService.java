package com.example.cart_service.service;

import com.ecommerce.common.events.CartAbandonedEvent;
import com.ecommerce.common.events.CartCheckedOutEvent;

public interface OutboxService {

    void saveCartCheckedOutEvent(CartCheckedOutEvent event);

    void saveCartAbandonedEvent(CartAbandonedEvent event);
}
