import { getMyPayments } from "./paymentService";

/*
 *  Add helper to wait for automatically-created payment
 */

function wait(milliseconds) {
  return new Promise((resolve) => {
    setTimeout(resolve, milliseconds);
  });
}

async function waitForPaymentForOrder(
  orderId,
  {
    interval = 1000,
    maxAttempts = 15
  } = {}
) {
  for (
    let attempt = 0;
    attempt < maxAttempts;
    attempt += 1
  ) {
    const response = await getMyPayments({
      page: 0,
      size: 100
    });

    const payments = response?.content || [];

    const payment = payments.find(
      (item) =>
        Number(item.orderId) === Number(orderId)
    );

    if (payment) {
      return payment;
    }

    if (attempt < maxAttempts - 1) {
      await wait(interval);
    }
  }

  throw new Error(
    "The payment could not be created for this order."
  );
}

export {
  waitForPaymentForOrder
};