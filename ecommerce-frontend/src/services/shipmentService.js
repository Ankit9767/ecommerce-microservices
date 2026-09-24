import {
  apiGet,
  apiPost
} from "./api";

function getShipment(shipmentId) {
  return apiGet(
    `/shipments/${shipmentId}`
  );
}

function getShipmentByOrderId(orderId) {
  return apiGet(
    `/shipments/order/${orderId}`
  );
}

function trackShipmentByOrderId(orderId) {
  return apiGet(
    `/shipments/track/order/${orderId}`
  );
}

function markInTransit(shipmentId) {
  return apiPost(
    `/shipments/${shipmentId}/in-transit`
  );
}

function markOutForDelivery(shipmentId) {
  return apiPost(
    `/shipments/${shipmentId}/out-for-delivery`
  );
}

function markDelivered(shipmentId) {
  return apiPost(
    `/shipments/${shipmentId}/deliver`
  );
}

function markFailed(shipmentId) {
  return apiPost(
    `/shipments/${shipmentId}/fail`
  );
}

function cancelShipment(shipmentId) {
  return apiPost(
    `/shipments/${shipmentId}/cancel`
  );
}

export {
  getShipment,
  getShipmentByOrderId,
  trackShipmentByOrderId,
  markInTransit,
  markOutForDelivery,
  markDelivered,
  markFailed,
  cancelShipment
};