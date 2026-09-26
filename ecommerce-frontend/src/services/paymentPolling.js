import { getPayment } from "./paymentService";

const TERMINAL_PAYMENT_STATUSES = [
  "SUCCESS",
  "FAILED",
  "CANCELLED",
  "REFUNDED"
];

function wait(milliseconds) {
  return new Promise((resolve) => {
    setTimeout(resolve, milliseconds);
  });
}

async function waitForPaymentCompletion(
  paymentId,
  {
    interval = 2000,
    maxAttempts = 30,
    onUpdate
  } = {}
) {
  for (let attempt = 0; attempt < maxAttempts; attempt += 1) {
    const payment = await getPayment(paymentId);

    if (onUpdate) {
      onUpdate(payment);
    }

    if (
      TERMINAL_PAYMENT_STATUSES.includes(
        payment.status
      )
    ) {
      return payment;
    }

    if (attempt < maxAttempts - 1) {
      await wait(interval);
    }
  }

  throw new Error(
    "Payment confirmation is taking longer than expected."
  );
}

export {
  waitForPaymentCompletion
};