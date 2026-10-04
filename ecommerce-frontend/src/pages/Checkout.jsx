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
  toRazorpayAmount,
} from "../services/razorpayService";

import "./styles/Checkout.css";

const CURRENCY_OPTIONS = [
  {
    value: "INR",
    label: "INR",
  },
  {
    value: "USD",
    label: "USD",
  },
];

const PAYMENT_METHOD_OPTIONS = [
  {
    value: "CARD",
    label: "Card",
  },
  {
    value: "UPI",
    label: "UPI",
  },
  {
    value: "NET_BANKING",
    label: "Net Banking",
  },
  {
    value: "WALLET",
    label: "Wallet",
  },
];

/* =========================
   Icons
========================= */

function CheckoutIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="checkout-icon">
      <path d="M4 5h2l1.4 9.2a2 2 0 0 0 2 1.7h7.9a2 2 0 0 0 1.9-1.5L21 8H7" />
      <circle cx="10" cy="19" r="1.2" />
      <circle cx="18" cy="19" r="1.2" />
    </svg>
  );
}

function ShippingIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="checkout-icon">
      <path d="M3.5 6.5h11v10h-11z" />
      <path d="M14.5 10h3.2l2.8 3v3.5h-6z" />
      <circle cx="7.5" cy="18" r="1.5" />
      <circle cx="17.5" cy="18" r="1.5" />
    </svg>
  );
}

function PaymentIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="checkout-icon">
      <rect x="3.5" y="5" width="17" height="14" rx="2" />
      <path d="M3.5 9h17" />
      <path d="M7 14h3" />
    </svg>
  );
}

function SummaryIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="checkout-icon">
      <path d="M6 3.5h12a1.5 1.5 0 0 1 1.5 1.5v14a1.5 1.5 0 0 1-1.5 1.5H6A1.5 1.5 0 0 1 4.5 19V5A1.5 1.5 0 0 1 6 3.5Z" />
      <path d="M8 8h8" />
      <path d="M8 12h8" />
      <path d="M8 16h5" />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="checkout-button-icon"
    >
      <path d="M19 12H5" />
      <path d="m11 6-6 6 6 6" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="checkout-small-icon">
      <rect x="5" y="10" width="14" height="10" rx="2" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
      <path d="M12 14v2" />
    </svg>
  );
}

function AlertIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="checkout-alert-icon">
      <path d="M12 4 21 19H3L12 4Z" />
      <path d="M12 9v5" />
      <path d="M12 17.2v.1" />
    </svg>
  );
}

function SpinnerIcon() {
  return <span className="checkout-spinner" aria-hidden="true" />;
}

/* =========================
   Checkout
========================= */

function Checkout() {
  const navigate = useNavigate();

  const { isAuthenticated, isLoading: isAuthLoading } = useAuth();

  const {
    items,
    totalAmount,
    totalItems,
    isLoading: isCartLoading,
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
    paymentMethod: "",
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  const [error, setError] = useState("");

  const [paymentStatus, setPaymentStatus] = useState("");

  const [isRedirectingToOrder, setIsRedirectingToOrder] = useState(false);

  const [redirectCountdown, setRedirectCountdown] = useState(5);

  const redirectToOrderAfterDelay = (orderId) => {
    if (!orderId || isRedirectingToOrder) {
      return;
    }

    setIsRedirectingToOrder(true);
    setRedirectCountdown(10);
    setIsSubmitting(false);

    let countdown = 10;

    const interval = window.setInterval(() => {
      countdown -= 1;

      if (countdown <= 0) {
        window.clearInterval(interval);
        navigate(`/orders/${orderId}`);
        return;
      }

      setRedirectCountdown(countdown);
    }, 1000);
  };

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));

    setError("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!isAuthenticated || items.length === 0 || isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setError("");
    setPaymentStatus("");

    try {
      setPaymentStatus("Creating your order...");

      const response = await createOrderFromCart({
        currency: form.currency,
        paymentMethod: form.paymentMethod,
        shippingAddress: {
          recipientName: form.recipientName,
          phone: form.phone,
          addressLine1: form.addressLine1,
          addressLine2: form.addressLine2,
          city: form.city,
          state: form.state,
          postalCode: form.postalCode,
          country: form.country,
        },
      });

      if (!response?.id) {
        throw new Error("The order was created, but no order ID was returned.");
      }

      const orderId = response.id;

      setPaymentStatus("Preparing your payment...");

      const payment = await waitForPaymentForOrder(orderId);

      if (!payment?.id) {
        throw new Error("The payment could not be created for this order.");
      }

      setPaymentStatus("Preparing Razorpay Checkout...");

      const checkout = await initializeCheckout(payment.id);

      if (!checkout?.providerKeyId || !checkout?.providerOrderId) {
        throw new Error("Payment checkout could not be initialized.");
      }

      setPaymentStatus("Opening secure payment...");

      await openRazorpayCheckout({
        key: checkout.providerKeyId,

        amount: toRazorpayAmount(checkout.amount),

        currency: checkout.currency,

        orderId: checkout.providerOrderId,

        name: "EcommerceHub",

        description: `Order #${checkout.orderId}`,

        prefill: {
          name: form.recipientName,
          contact: form.phone,
        },

        notes: {
          order_id: String(checkout.orderId),
          payment_id: String(checkout.paymentId),
        },

        onSuccess: async () => {
          try {
            setPaymentStatus("Confirming your payment...");

            const completedPayment = await waitForPaymentCompletion(
              checkout.paymentId,
              {
                onUpdate: (updatedPayment) => {
                  setPaymentStatus(`Payment status: ${updatedPayment.status}`);
                },
              },
            );

            if (completedPayment.status !== "SUCCESS") {
              throw new Error("The payment was not completed successfully.");
            }

            navigate(`/orders/${checkout.orderId}`);
          } catch (paymentError) {
            setError(paymentError.message || "Unable to confirm the payment.");

            setPaymentStatus("");
            setIsSubmitting(false);
          }
        },

        onFailure: () => {
          setError(
            "The Razorpay payment was not completed. Please close the payment window to continue to your order.",
          );

          setPaymentStatus("");

          // Do NOT start the redirect here.
          // Razorpay may still be open.
        },

        onDismiss: () => {
          setError(
            "Payment was cancelled or the Razorpay checkout was closed. You can complete the payment from the Orders page.",
          );

          setPaymentStatus("");
          redirectToOrderAfterDelay(checkout.orderId);
        },
      });
    } catch (requestError) {
      setError(
        requestError.message || "Unable to place your order. Please try again.",
      );

      setPaymentStatus("");
      setIsSubmitting(false);
    }
  };

  if (isAuthLoading || isCartLoading) {
    return (
      <main className="page checkout-page">
        <div className="container">
          <div className="checkout-state checkout-loading-state">
            <div className="checkout-state-icon">
              <CheckoutIcon />
            </div>

            <h1 className="page-title">Checkout</h1>

            <p>Preparing your checkout...</p>

            <SpinnerIcon />
          </div>
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return (
      <main className="page checkout-page">
        <div className="container">
          <div className="checkout-state">
            <div className="checkout-state-icon">
              <CheckoutIcon />
            </div>

            <h1 className="page-title">Checkout</h1>

            <p>Please sign in before checking out.</p>

            <Link className="button checkout-state-button" to="/login">
              <span>Sign In</span>
            </Link>
          </div>
        </div>
      </main>
    );
  }

  if (items.length === 0) {
    return (
      <main className="page checkout-page">
        <div className="container">
          <div className="checkout-state">
            <div className="checkout-state-icon">
              <CheckoutIcon />
            </div>

            <h1 className="page-title">Checkout</h1>

            <p>Your cart is empty.</p>

            <Link className="button checkout-state-button" to="/products">
              <span>Continue Shopping</span>
            </Link>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="page checkout-page">
      <div className="container">
        <div className="checkout-page-header">
          <Link className="checkout-back-link" to="/cart">
            <ArrowIcon />
            <span>Back to Cart</span>
          </Link>

          <div className="checkout-header-content">
            <div className="checkout-heading-icon">
              <CheckoutIcon />
            </div>

            <div>
              <p className="checkout-eyebrow">EcommerceHub</p>
              <h1 className="page-title">Checkout</h1>
            </div>
          </div>
        </div>

        <div className="checkout-layout">
          <section className="checkout-form-section">
            <div className="checkout-section-heading">
              <div className="checkout-section-icon">
                <ShippingIcon />
              </div>

              <div>
                <p className="checkout-section-eyebrow">Delivery details</p>

                <h2>Shipping Address</h2>
              </div>
            </div>

            <form className="checkout-form" onSubmit={handleSubmit}>
              <section className="checkout-section">
                <div className="checkout-fields">
                  <FormField
                    label="Recipient Name"
                    name="recipientName"
                    value={form.recipientName}
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
                    value={form.addressLine1}
                    onChange={handleChange}
                    required
                    maxLength={200}
                    disabled={isSubmitting}
                  />

                  <FormField
                    label="Address Line 2"
                    name="addressLine2"
                    value={form.addressLine2}
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
                      value={form.postalCode}
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
                </div>
              </section>

              <section className="checkout-section checkout-payment-section">
                <div className="checkout-section-heading">
                  <div className="checkout-section-icon">
                    <PaymentIcon />
                  </div>

                  <div>
                    <p className="checkout-section-eyebrow">Secure payment</p>

                    <h2>Payment Information</h2>
                  </div>
                </div>

                <div className="checkout-fields">
                  <div className="checkout-field">
                    <label htmlFor="currency">Currency</label>

                    <select
                      id="currency"
                      name="currency"
                      value={form.currency}
                      onChange={handleChange}
                      required
                      disabled={isSubmitting}
                    >
                      <option value="">Select currency</option>

                      {CURRENCY_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="checkout-field">
                    <label htmlFor="paymentMethod">Payment Method</label>

                    <select
                      id="paymentMethod"
                      name="paymentMethod"
                      value={form.paymentMethod}
                      onChange={handleChange}
                      required
                      disabled={isSubmitting}
                    >
                      <option value="">Select payment method</option>

                      {PAYMENT_METHOD_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="checkout-secure-note">
                  <LockIcon />

                  <span>
                    Your payment will be completed securely through Razorpay.
                  </span>
                </div>
              </section>

              {paymentStatus && (
                <div className="checkout-status" role="status">
                  <div className="checkout-status-icon">
                    <SpinnerIcon />
                  </div>

                  <div>
                    <strong>Payment processing</strong>
                    <p>{paymentStatus}</p>
                  </div>
                </div>
              )}

              {error && (
                <div className="checkout-error" role="alert">
                  <div className="checkout-alert-icon-wrap">
                    <AlertIcon />
                  </div>

                  <p>{error}</p>
                </div>
              )}

              {isRedirectingToOrder && (
                <div
                  className="checkout-redirect-status"
                  role="status"
                  aria-live="polite"
                >
                  <span
                    className="checkout-redirect-spinner"
                    aria-hidden="true"
                  />

                  <div className="checkout-redirect-content">
                    <strong>Redirecting to your order</strong>
                    <span>
                      Opening your order in {redirectCountdown} second
                      {redirectCountdown !== 1 ? "s" : ""}...
                    </span>
                  </div>
                </div>
              )}

              <button
                type="submit"
                className="button checkout-submit"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    <SpinnerIcon />
                    <span>Processing Payment...</span>
                  </>
                ) : (
                  <>
                    <span>Place Order &amp; Pay</span>
                    <CheckoutIcon />
                  </>
                )}
              </button>
            </form>
          </section>

          <aside className="checkout-summary">
            <div className="checkout-summary-glow" />

            <div className="checkout-summary-heading">
              <div className="checkout-summary-icon">
                <SummaryIcon />
              </div>

              <div>
                <p className="checkout-summary-eyebrow">Your purchase</p>

                <h2>Order Summary</h2>
              </div>
            </div>

            <div className="checkout-summary-details">
              <div className="checkout-summary-row">
                <span>Items</span>
                <strong>{totalItems}</strong>
              </div>

              <div className="checkout-summary-row checkout-summary-total-row">
                <span>Total Amount</span>

                <strong>
                  {form.currency || "—"} {Number(totalAmount).toFixed(2)}
                </strong>
              </div>
            </div>

            <div className="checkout-summary-footer">
              <div className="checkout-summary-footer-dot" />
              <span>Secure checkout</span>
            </div>
          </aside>
        </div>
      </div>
    </main>
  );
}

export default Checkout;
