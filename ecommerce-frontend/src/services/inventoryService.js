import { apiGet, apiPost } from "./api";

function createInventory(request) {
  return apiPost("/inventory", request);
}

function getInventory(productId) {
  return apiGet(`/inventory/${productId}`);
}

function increaseStock(productId, quantity) {
  return apiPost(
    `/inventory/${productId}/increase`,
    {
      quantity
    }
  );
}

function decreaseStock(productId, quantity) {
  return apiPost(
    `/inventory/${productId}/decrease`,
    {
      quantity
    }
  );
}

function reserveStock(
  productId,
  quantity,
  reservationId = null
) {
  return apiPost(
    `/inventory/${productId}/reserve`,
    {
      quantity,
      reservationId
    }
  );
}

function releaseStock(
  productId,
  quantity,
  reservationId = null
) {
  return apiPost(
    `/inventory/${productId}/release`,
    {
      quantity,
      reservationId
    }
  );
}

function confirmReservation(
  productId,
  quantity,
  reservationId = null
) {
  return apiPost(
    `/inventory/${productId}/confirm`,
    {
      quantity,
      reservationId
    }
  );
}

export {
  createInventory,
  getInventory,
  increaseStock,
  decreaseStock,
  reserveStock,
  releaseStock,
  confirmReservation
};