import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import {
  cancelOrder,
  getOrder
} from "../services/orderService";

import "./styles/OrderDetails.css";

function OrderDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const { isAuthenticated, isLoading: isAuthLoading } = useAuth();

  const [order, setOrder] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isCancelling, setIsCancelling] = useState(false);

  const [error, setError] = useState("");
  const [cancelError, setCancelError] = useState("");

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
  }, [id, isAuthenticated, isAuthLoading]);

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
      const response = await cancelOrder(order.id);
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

          <span className="order-details-status">
            {order.status}
          </span>
        </div>

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
                        Quantity: {item.quantity}
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
                  {order.shippingAddress?.recipientName}
                </strong>

                <span>
                  {order.shippingAddress?.phone}
                </span>

                <span>
                  {order.shippingAddress?.addressLine1}
                </span>

                {order.shippingAddress?.addressLine2 && (
                  <span>
                    {order.shippingAddress.addressLine2}
                  </span>
                )}

                <span>
                  {order.shippingAddress?.city},{" "}
                  {order.shippingAddress?.state}
                </span>

                <span>
                  {order.shippingAddress?.postalCode}
                </span>

                <span>
                  {order.shippingAddress?.country}
                </span>
              </address>
            </div>
          </section>

          <aside className="order-details-summary">
            <h2>Order Summary</h2>

            <div className="order-summary-row">
              <span>Status</span>
              <strong>{order.status}</strong>
            </div>

            <div className="order-summary-row">
              <span>Payment Method</span>
              <strong>
                {order.paymentMethod}
              </strong>
            </div>

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
          </aside>
        </div>
      </div>
    </main>
  );
}

export default OrderDetails;