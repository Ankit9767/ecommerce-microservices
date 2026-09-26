import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import FormField from "../components/FormField";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

import { createOrderFromCart } from "../services/orderService";
import { initializeCheckout } from "../services/paymentService";
import { waitForPaymentForOrder } from "../services/orderPaymentService";
import { waitForPaymentCompletion } from "../services/paymentPolling";
import {
  openRazorpayCheckout,
  toRazorpayAmount
} from "../services/razorpayService";

import "./styles/Checkout.css";

const CURRENCY_OPTIONS = [
  {
    value: "INR",
    label: "INR"
  },
  {
    value: "USD",
    label: "USD"
  }
];

const PAYMENT_METHOD_OPTIONS = [
  {
    value: "CARD",
    label: "Card"
  },
  {
    value: "UPI",
    label: "UPI"
  },
  {
    value: "NET_BANKING",
    label: "Net Banking"
  },
  {
    value: "WALLET",
    label: "Wallet"
  }
];

function Checkout() {
  const navigate = useNavigate();

  const {
    isAuthenticated,
    isLoading: isAuthLoading
  } = useAuth();

  const {
    items,
    totalAmount,
    totalItems,
    isLoading: isCartLoading
  } = useCart();

  const [form, setForm] = useState({
    recipientName: "",
    phone: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: "",
    currency: "INR",
    paymentMethod: ""
  });

  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const [error, setError] = useState("");

  const [paymentStatus, setPaymentStatus] =
    useState("");

  const handleChange = (event) => {
    const {
      name,
      value
    } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value
    }));

    setError("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (
      !isAuthenticated ||
      items.length === 0 ||
      isSubmitting
    ) {
      return;
    }

    setIsSubmitting(true);
    setError("");
    setPaymentStatus("");

    try {
      /*
       * --------------------------------------------------
       * STEP 1
       *
       * Create the order.
       *
       * The frontend does NOT create the payment.
       *
       * The Order Service publishes OrderCreatedEvent
       * and the Payment Service automatically creates
       * the payment internally.
       * --------------------------------------------------
       */

      setPaymentStatus(
        "Creating your order..."
      );

      const response =
        await createOrderFromCart({
          currency: form.currency,
          paymentMethod:
            form.paymentMethod,
          shippingAddress: {
            recipientName:
              form.recipientName,
            phone: form.phone,
            addressLine1:
              form.addressLine1,
            addressLine2:
              form.addressLine2,
            city: form.city,
            state: form.state,
            postalCode:
              form.postalCode,
            country: form.country
          }
        });

      if (!response?.id) {
        throw new Error(
          "The order was created, but no order ID was returned."
        );
      }

      const orderId = response.id;

      /*
       * --------------------------------------------------
       * STEP 2
       *
       * Wait for the Payment Service to automatically
       * create the payment for this order.
       * --------------------------------------------------
       */

      setPaymentStatus(
        "Preparing your payment..."
      );

      const payment =
        await waitForPaymentForOrder(
          orderId
        );

      if (!payment?.id) {
        throw new Error(
          "The payment could not be created for this order."
        );
      }

      /*
       * --------------------------------------------------
       * STEP 3
       *
       * Initialize Razorpay checkout.
       *
       * This calls:
       *
       * POST /api/payments/{paymentId}/checkout
       * --------------------------------------------------
       */

      setPaymentStatus(
        "Preparing Razorpay Checkout..."
      );

      const checkout =
        await initializeCheckout(
          payment.id
        );

      if (
        !checkout?.providerKeyId ||
        !checkout?.providerOrderId
      ) {
        throw new Error(
          "Payment checkout could not be initialized."
        );
      }

      /*
       * --------------------------------------------------
       * STEP 4
       *
       * Open Razorpay Checkout.
       * --------------------------------------------------
       */

      setPaymentStatus(
        "Opening secure payment..."
      );

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
          `Order #${checkout.orderId}`,

        prefill: {
          name: form.recipientName,
          contact: form.phone
        },

        notes: {
          order_id: String(
            checkout.orderId
          ),
          payment_id: String(
            checkout.paymentId
          )
        },

        /*
         * IMPORTANT:
         *
         * This callback does NOT update our payment.
         *
         * Razorpay's webhook is responsible for
         * updating the payment in our backend.
         */
        onSuccess: async () => {
          try {
            setPaymentStatus(
              "Confirming your payment..."
            );

            /*
             * --------------------------------------------------
             * STEP 5
             *
             * Wait until the backend receives the Razorpay
             * webhook and updates our local payment.
             * --------------------------------------------------
             */

            const completedPayment =
              await waitForPaymentCompletion(
                checkout.paymentId,
                {
                  onUpdate: (
                    updatedPayment
                  ) => {
                    setPaymentStatus(
                      `Payment status: ${updatedPayment.status}`
                    );
                  }
                }
              );

            /*
             * Only SUCCESS allows us to continue.
             */

            if (
              completedPayment.status !==
              "SUCCESS"
            ) {
              throw new Error(
                "The payment was not completed successfully."
              );
            }

            /*
             * Payment is confirmed by the backend.
             *
             * Now we can show the order details.
             */

            navigate(
              `/orders/${checkout.orderId}`
            );
          } catch (paymentError) {
            setError(
              paymentError.message ||
                "Unable to confirm the payment."
            );

            setPaymentStatus("");
            setIsSubmitting(false);
          }
        },

        onFailure: () => {
          setError(
            "The Razorpay payment failed. Please try again."
          );

          setPaymentStatus("");
          setIsSubmitting(false);
        },

        onDismiss: () => {
          setError(
            "Payment was cancelled or the Razorpay checkout was closed."
          );

          setPaymentStatus("");
          setIsSubmitting(false);
        }
      });
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to place your order. Please try again."
      );

      setPaymentStatus("");
      setIsSubmitting(false);
    }
  };

  if (
    isAuthLoading ||
    isCartLoading
  ) {
    return (
      <main className="page">
        <div className="container">
          <p className="checkout-state">
            Loading checkout...
          </p>
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return (
      <main className="page">
        <div className="container">
          <div className="checkout-state">
            <h1 className="page-title">
              Checkout
            </h1>

            <p>
              Please sign in before checking out.
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

  if (items.length === 0) {
    return (
      <main className="page">
        <div className="container">
          <div className="checkout-state">
            <h1 className="page-title">
              Checkout
            </h1>

            <p>
              Your cart is empty.
            </p>

            <Link
              className="button"
              to="/products"
            >
              Continue Shopping
            </Link>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="page">
      <div className="container">
        <Link
          className="checkout-back-link"
          to="/cart"
        >
          ← Back to Cart
        </Link>

        <div className="checkout-layout">
          <section className="checkout-form-section">
            <h1 className="page-title">
              Checkout
            </h1>

            <form
              className="checkout-form"
              onSubmit={handleSubmit}
            >
              <section className="checkout-section">
                <h2>
                  Shipping Address
                </h2>

                <FormField
                  label="Recipient Name"
                  name="recipientName"
                  value={
                    form.recipientName
                  }
                  onChange={handleChange}
                  required
                  maxLength={100}
                  disabled={isSubmitting}
                />

                <FormField
                  label="Phone"
                  name="phone"
                  value={form.phone}
                  onChange={handleChange}
                  required
                  maxLength={30}
                  disabled={isSubmitting}
                />

                <FormField
                  label="Address Line 1"
                  name="addressLine1"
                  value={
                    form.addressLine1
                  }
                  onChange={handleChange}
                  required
                  maxLength={200}
                  disabled={isSubmitting}
                />

                <FormField
                  label="Address Line 2"
                  name="addressLine2"
                  value={
                    form.addressLine2
                  }
                  onChange={handleChange}
                  maxLength={200}
                  disabled={isSubmitting}
                />

                <div className="checkout-form-grid">
                  <FormField
                    label="City"
                    name="city"
                    value={form.city}
                    onChange={handleChange}
                    required
                    maxLength={100}
                    disabled={isSubmitting}
                  />

                  <FormField
                    label="State"
                    name="state"
                    value={form.state}
                    onChange={handleChange}
                    required
                    maxLength={100}
                    disabled={isSubmitting}
                  />

                  <FormField
                    label="Postal Code"
                    name="postalCode"
                    value={
                      form.postalCode
                    }
                    onChange={handleChange}
                    required
                    maxLength={20}
                    disabled={isSubmitting}
                  />

                  <FormField
                    label="Country"
                    name="country"
                    value={form.country}
                    onChange={handleChange}
                    required
                    maxLength={2}
                    disabled={isSubmitting}
                  />
                </div>
              </section>

              <section className="checkout-section">
                <h2>
                  Payment Information
                </h2>

                <div className="checkout-field">
                  <label htmlFor="currency">
                    Currency
                  </label>

                  <select
                    id="currency"
                    name="currency"
                    value={form.currency}
                    onChange={handleChange}
                    required
                    disabled={isSubmitting}
                  >
                    <option value="">
                      Select currency
                    </option>

                    {CURRENCY_OPTIONS.map(
                      (option) => (
                        <option
                          key={option.value}
                          value={option.value}
                        >
                          {option.label}
                        </option>
                      )
                    )}
                  </select>
                </div>

                <div className="checkout-field">
                  <label htmlFor="paymentMethod">
                    Payment Method
                  </label>

                  <select
                    id="paymentMethod"
                    name="paymentMethod"
                    value={
                      form.paymentMethod
                    }
                    onChange={handleChange}
                    required
                    disabled={isSubmitting}
                  >
                    <option value="">
                      Select payment method
                    </option>

                    {PAYMENT_METHOD_OPTIONS.map(
                      (option) => (
                        <option
                          key={option.value}
                          value={
                            option.value
                          }
                        >
                          {option.label}
                        </option>
                      )
                    )}
                  </select>
                </div>
              </section>

              {paymentStatus && (
                <div
                  className="checkout-status"
                  role="status"
                >
                  <p>
                    {paymentStatus}
                  </p>
                </div>
              )}

              {error && (
                <p
                  className="checkout-error"
                  role="alert"
                >
                  {error}
                </p>
              )}

              <button
                type="submit"
                className="button checkout-submit"
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? "Processing Payment..."
                  : "Place Order & Pay"}
              </button>
            </form>
          </section>

          <aside className="checkout-summary">
            <h2>
              Order Summary
            </h2>

            <div className="checkout-summary-row">
              <span>Items</span>

              <span>
                {totalItems}
              </span>
            </div>

            <div className="checkout-summary-row checkout-summary-total">
              <span>Total</span>

              <span>
                {form.currency || "—"}{" "}
                {Number(
                  totalAmount
                ).toFixed(2)}
              </span>
            </div>
          </aside>
        </div>
      </div>
    </main>
  );
}

export default Checkout;