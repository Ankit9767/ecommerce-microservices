import { apiGet, apiPost } from "./api";

/*
 * Customer/Admin depending on backend authorization.
 */
function getPayment(paymentId) {
  return apiGet(`/payments/${paymentId}`);
}

/*
 * Customer payment history.
 */
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

/*
 * Customer-accessible according to the supplied controller.
 */
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

/*
 * Customer checkout operation.
 *
 * The payment itself has already been created automatically
 * by the Payment Service after the order-created event.
 */
function initializeCheckout(paymentId) {
  return apiPost(
    `/payments/${paymentId}/checkout`
  );
}

/*
 * Do not expose createPayment here for the customer checkout flow.
 *
 * Payment creation happens automatically through:
 *
 * OrderCreatedEvent
 *      ↓
 * createPaymentInternal()
 *
 * Mock complete/fail endpoints are also intentionally omitted
 * from the customer flow because Razorpay is being used.
 */

export {
  getPayment,
  getMyPayments,
  getPaymentsByStatus,
  initializeCheckout
};