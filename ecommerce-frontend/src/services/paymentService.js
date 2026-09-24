import { apiGet, apiPost } from "./api";

function createPayment(orderId) {
  return apiPost("/payments", {
    orderId
  });
}

function getPayment(paymentId) {
  return apiGet(`/payments/${paymentId}`);
}

function getMyPayments({
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

  return apiGet(`/payments/my?${params.toString()}`);
}

function getPaymentsByStatus(
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

  return apiGet(
    `/payments/status/${status}?${params.toString()}`
  );
}

function initializeCheckout(paymentId) {
  return apiPost(`/payments/${paymentId}/checkout`);
}

function completeMockPayment(paymentId) {
  return apiPost(
    `/payments/${paymentId}/mock/complete`
  );
}

function failMockPayment(paymentId) {
  return apiPost(
    `/payments/${paymentId}/mock/fail`
  );
}

export {
  createPayment,
  getPayment,
  getMyPayments,
  getPaymentsByStatus,
  initializeCheckout,
  completeMockPayment,
  failMockPayment
};