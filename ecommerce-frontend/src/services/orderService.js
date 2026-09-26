import {
  apiGet,
  apiPost,
  apiPut,
  apiDelete
} from "./api";

function createOrder(request) {
  return apiPost("/orders", request);
}

function createOrderFromCart({
  currency,
  paymentMethod,
  shippingAddress
}) {
  return apiPost("/orders/from-cart", {
    currency,
    paymentMethod,
    shippingAddress
  });
}

function getOrder(orderId) {
  return apiGet(`/orders/${orderId}`);
}

function getMyOrders({
  page = 0,
  size = 20,
  sort
} = {}) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(`/orders/my?${params.toString()}`);
}

function getOrdersByStatus(
  status,
  {
    page = 0,
    size = 20,
    sort
  } = {}
) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(`/orders/status/${status}?${params.toString()}`);
}

function updateOrder(orderId, request) {
  return apiPut(`/orders/${orderId}`, request);
}

function cancelOrder(orderId) {
  return apiDelete(`/orders/${orderId}`);
}

function checkReviewEligibility(
  orderId,
  productId,
  userId
) {
  const params = new URLSearchParams();

  params.set("productId", productId);
  params.set("userId", userId);

  return apiGet(
    `/orders/${orderId}/review-eligibility?${params.toString()}`
  );
}

/*
 * Admin-only.
 */
function getAllOrders({
  page = 0,
  size = 20,
  sort
} = {}) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(`/orders?${params.toString()}`);
}

export {
  createOrder,
  createOrderFromCart,
  getOrder,
  getMyOrders,
  getOrdersByStatus,
  updateOrder,
  cancelOrder,
  checkReviewEligibility,
  getAllOrders
};