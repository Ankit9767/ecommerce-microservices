package com.example.shipping_service.service;

import com.ecommerce.common.events.ShipmentEvent;

public interface ShippingOutboxService {

    void saveShipmentEvent(ShipmentEvent event);
}