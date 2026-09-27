import React, {
  useCallback,
  useEffect,
  useState
} from "react";
import {
  Link,
  useParams
} from "react-router-dom";

import { useAuth } from "../context/AuthContext";

import {
  cancelOrder,
  getOrder
} from "../services/orderService";

import {
  getMyPayments,
  getPayment,
  initializeCheckout
} from "../services/paymentService";

import {
  openRazorpayCheckout,
  toRazorpayAmount
} from "../services/razorpayService";

import "./styles/OrderDetails.css";

const PAYMENT_POLL_INTERVAL = 1500;
const PAYMENT_POLL_ATTEMPTS = 20;

function OrderDetails() {
  const { id } = useParams();

  const {
    isAuthenticated,
    isLoading: isAuthLoading
  } = useAuth();

  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(null);

  const [isLoading, setIsLoading] = useState(true);
  const [isPaymentLoading, setIsPaymentLoading] =
    useState(false);
  const [isProcessingPayment, setIsProcessingPayment] =
    useState(false);
  const [isCancelling, setIsCancelling] =
    useState(false);

  const [error, setError] = useState("");
  const [paymentError, setPaymentError] =
    useState("");
  const [paymentMessage, setPaymentMessage] =
    useState("");
  const [cancelError, setCancelError] =
    useState("");

  /*
   * Load the order.
   */
  useEffect(() => {
    let isMounted = true;

    async function loadOrder() {
      if (!isAuthenticated || !id) {
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError("");

      try {
        const response = await getOrder(id);

        if (!isMounted) {
          return;
        }

        setOrder(response);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        if (requestError.status === 404) {
          setError("Order not found.");
        } else {
          setError(
            requestError.message ||
              "Unable to load this order."
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    if (!isAuthLoading) {
      loadOrder();
    }

    return () => {
      isMounted = false;
    };
  }, [
    id,
    isAuthenticated,
    isAuthLoading
  ]);

  /*
   * Find the payment created by the backend's
   * OrderCreatedEvent.
   *
   * There is no /payments/by-order/{id} endpoint,
   * so we use the existing customer payment endpoint.
   */
  const findPaymentForOrder = useCallback(
    async () => {
      const response = await getMyPayments({
        page: 0,
        size: 100
      });

      const payments = response?.content || [];

      const matchingPayment = payments.find(
        (item) =>
          Number(item.orderId) === Number(id)
      );

      if (matchingPayment) {
        setPayment(matchingPayment);
      }

      return matchingPayment || null;
    },
    [id]
  );

  /*
   * The payment is created asynchronously by the backend
   * after the order-created event.
   *
   * Therefore it might not exist immediately after
   * createOrderFromCart().
   */
  useEffect(() => {
    let isMounted = true;

    async function discoverPayment() {
      if (
        !order ||
        order.status !== "PENDING_PAYMENT"
      ) {
        return;
      }

      setIsPaymentLoading(true);
      setPaymentError("");

      for (
        let attempt = 0;
        attempt < PAYMENT_POLL_ATTEMPTS;
        attempt += 1
      ) {
        try {
          const foundPayment =
            await findPaymentForOrder();

          if (foundPayment) {
            if (isMounted) {
              setPayment(foundPayment);
            }

            return;
          }
        } catch (requestError) {
          if (isMounted) {
            setPaymentError(
              requestError.message ||
                "Unable to find the payment for this order."
            );
          }

          return;
        }

        await new Promise((resolve) => {
          setTimeout(
            resolve,
            PAYMENT_POLL_INTERVAL
          );
        });
      }

      if (isMounted) {
        setPaymentError(
          "Your payment is still being prepared. Please refresh the page and try again."
        );
      }
    }

    if (
      isAuthenticated &&
      order?.status === "PENDING_PAYMENT"
    ) {
      discoverPayment().finally(() => {
        if (isMounted) {
          setIsPaymentLoading(false);
        }
      });
    }

    return () => {
      isMounted = false;
    };
  }, [
    order,
    isAuthenticated,
    findPaymentForOrder
  ]);

  /*
   * After Razorpay returns control to the browser,
   * do NOT assume payment succeeded.
   *
   * The backend Razorpay webhook is the source of truth.
   */
  const waitForPaymentResult = useCallback(
    async (paymentId) => {
      setPaymentMessage(
        "Payment submitted. Waiting for confirmation..."
      );

      setPaymentError("");

      for (
        let attempt = 0;
        attempt < PAYMENT_POLL_ATTEMPTS;
        attempt += 1
      ) {
        try {
          const latestPayment =
            await getPayment(paymentId);

          setPayment(latestPayment);

          if (
            latestPayment.status ===
            "SUCCESS"
          ) {
            setPaymentMessage(
              "Payment completed successfully."
            );

            try {
              const latestOrder =
                await getOrder(id);

              setOrder(latestOrder);
            } catch {
              // Payment is already confirmed.
            }

            return latestPayment;
          }

          if (
            latestPayment.status === "FAILED" ||
            latestPayment.status === "CANCELLED"
          ) {
            setPaymentError(
              latestPayment.failureReason ||
                "Payment was not completed."
            );

            return latestPayment;
          }
        } catch (requestError) {
          setPaymentError(
            requestError.message ||
              "Unable to check payment status."
          );

          return null;
        }

        await new Promise((resolve) => {
          setTimeout(
            resolve,
            PAYMENT_POLL_INTERVAL
          );
        });
      }

      setPaymentMessage(
        "Payment is still being processed. The order will update after the backend receives the payment confirmation."
      );

      return null;
    },
    [id]
  );

  /*
   * Start Razorpay checkout ONLY when the customer
   * clicks the Complete Payment button.
   */
  const handleCompletePayment =
    useCallback(async () => {
      if (
        !payment?.id ||
        payment.status !== "PENDING" ||
        isProcessingPayment
      ) {
        return;
      }

      setIsProcessingPayment(true);
      setPaymentError("");
      setPaymentMessage("");

      try {
        const checkout =
          await initializeCheckout(
            payment.id
          );

        if (
          !checkout?.providerKeyId ||
          !checkout?.providerOrderId
        ) {
          throw new Error(
            "Payment checkout information is incomplete."
          );
        }

        await openRazorpayCheckout({
          key: checkout.providerKeyId,

          amount: toRazorpayAmount(
            checkout.amount
          ),

          currency: checkout.currency,

          orderId:
            checkout.providerOrderId,

          name: "EcommerceHub",

          description:
            `Order #${id}`,

          onSuccess: async () => {
            /*
             * Razorpay's browser callback does NOT
             * mark the payment as successful.
             *
             * The backend webhook does that.
             */
            await waitForPaymentResult(
              payment.id
            );

            setIsProcessingPayment(false);
          },

          onFailure: (response) => {
            setPaymentError(
              response?.error?.description ||
                "Razorpay reported that the payment failed."
            );

            setIsProcessingPayment(false);
          },

          onDismiss: () => {
            setPaymentMessage(
              "Payment window was closed. Your order is still awaiting payment."
            );

            setIsProcessingPayment(false);
          }
        });
      } catch (requestError) {
        setPaymentError(
          requestError.message ||
            "Unable to start the payment."
        );

        setIsProcessingPayment(false);
      }
    }, [
      id,
      payment,
      isProcessingPayment,
      waitForPaymentResult
    ]);

  /*
   * Cancel order.
   */
  const handleCancel = async () => {
    if (!order?.id || isCancelling) {
      return;
    }

    const confirmed = window.confirm(
      "Are you sure you want to cancel this order?"
    );

    if (!confirmed) {
      return;
    }

    setIsCancelling(true);
    setCancelError("");

    try {
      const response = await cancelOrder(
        order.id
      );

      setOrder(response);
    } catch (requestError) {
      setCancelError(
        requestError.message ||
          "Unable to cancel this order."
      );
    } finally {
      setIsCancelling(false);
    }
  };

  if (isAuthLoading || isLoading) {
    return (
      <main className="page">
        <div className="container">
          <p className="order-details-state">
            Loading order...
          </p>
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return (
      <main className="page">
        <div className="container">
          <div className="order-details-state">
            <h1 className="page-title">
              Order Details
            </h1>

            <p>
              Please sign in to view your order.
            </p>

            <Link
              className="button"
              to="/login"
            >
              Sign In
            </Link>
          </div>
        </div>
      </main>
    );
  }

  if (error) {
    return (
      <main className="page">
        <div className="container">
          <div className="order-details-state">
            <p className="order-details-error">
              {error}
            </p>

            <Link
              className="button"
              to="/orders"
            >
              Back to Orders
            </Link>
          </div>
        </div>
      </main>
    );
  }

  if (!order) {
    return null;
  }

  const isPaymentPending =
    order.status === "PENDING_PAYMENT";

  const canCancel =
    ![
      "CANCELLED",
      "SHIPPED",
      "DELIVERED"
    ].includes(order.status);

  return (
    <main className="page">
      <div className="container">
        <div className="order-details-header">
          <div>
            <Link
              className="order-back-link"
              to="/orders"
            >
              ← Back to Orders
            </Link>

            <h1 className="page-title">
              Order #{order.id}
            </h1>
          </div>

          <span
            className={`order-details-status ${
              isPaymentPending
                ? "order-details-status-pending"
                : ""
            }`}
          >
            {order.status}
          </span>
        </div>

        {isPaymentPending && (
          <section className="order-payment-section">
            <div>
              <h2>Payment Required</h2>

              <p>
                Your order has been created, but
                payment has not been completed yet.
              </p>

              {isPaymentLoading && (
                <p className="order-payment-message">
                  Preparing your payment...
                </p>
              )}

              {paymentMessage && (
                <p className="order-payment-message">
                  {paymentMessage}
                </p>
              )}

              {paymentError && (
                <p
                  className="order-payment-error"
                  role="alert"
                >
                  {paymentError}
                </p>
              )}
            </div>

            <button
              type="button"
              className="button order-complete-payment-button"
              onClick={handleCompletePayment}
              disabled={
                isPaymentLoading ||
                isProcessingPayment ||
                !payment?.id ||
                payment?.status !== "PENDING"
              }
            >
              {isProcessingPayment
                ? "Processing Payment..."
                : "Complete Payment"}
            </button>
          </section>
        )}

        <div className="order-details-layout">
          <section className="order-details-main">
            <div className="order-details-section">
              <h2>Items</h2>

              <div className="order-items">
                {order.items?.map((item) => (
                  <article
                    className="order-item"
                    key={item.id}
                  >
                    <div>
                      <h3>
                        {item.productName}
                      </h3>

                      {item.sku && (
                        <p>
                          SKU: {item.sku}
                        </p>
                      )}

                      <p>
                        Quantity:{" "}
                        {item.quantity}
                      </p>
                    </div>

                    <div className="order-item-price">
                      <span>
                        {order.currency}{" "}
                        {Number(
                          item.unitPrice
                        ).toFixed(2)}{" "}
                        each
                      </span>

                      <strong>
                        {order.currency}{" "}
                        {Number(
                          item.lineTotal
                        ).toFixed(2)}
                      </strong>
                    </div>
                  </article>
                ))}
              </div>
            </div>

            <div className="order-details-section">
              <h2>Shipping Address</h2>

              <address>
                <strong>
                  {
                    order.shippingAddress
                      ?.recipientName
                  }
                </strong>

                <span>
                  {
                    order.shippingAddress
                      ?.phone
                  }
                </span>

                <span>
                  {
                    order.shippingAddress
                      ?.addressLine1
                  }
                </span>

                {order.shippingAddress
                  ?.addressLine2 && (
                  <span>
                    {
                      order.shippingAddress
                        .addressLine2
                    }
                  </span>
                )}

                <span>
                  {
                    order.shippingAddress
                      ?.city
                  }
                  ,{" "}
                  {
                    order.shippingAddress
                      ?.state
                  }
                </span>

                <span>
                  {
                    order.shippingAddress
                      ?.postalCode
                  }
                </span>

                <span>
                  {
                    order.shippingAddress
                      ?.country
                  }
                </span>
              </address>
            </div>
          </section>

          <aside className="order-details-summary">
            <h2>Order Summary</h2>

            <div className="order-summary-row">
              <span>Status</span>

              <strong>
                {order.status}
              </strong>
            </div>

            <div className="order-summary-row">
              <span>Payment Method</span>

              <strong>
                {order.paymentMethod}
              </strong>
            </div>

            {payment && (
              <div className="order-summary-row">
                <span>Payment Status</span>

                <strong>
                  {payment.status}
                </strong>
              </div>
            )}

            <div className="order-summary-row order-summary-total">
              <span>Total</span>

              <strong>
                {order.currency}{" "}
                {Number(
                  order.totalAmount
                ).toFixed(2)}
              </strong>
            </div>

            {cancelError && (
              <p
                className="order-details-error"
                role="alert"
              >
                {cancelError}
              </p>
            )}

            {canCancel && (
              <button
                type="button"
                className="button order-cancel-button"
                onClick={handleCancel}
                disabled={isCancelling}
              >
                {isCancelling
                  ? "Cancelling..."
                  : "Cancel Order"}
              </button>
            )}
          </aside>
        </div>
      </div>
    </main>
  );
}

export default OrderDetails;