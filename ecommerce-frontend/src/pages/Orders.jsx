
import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import { getMyOrders } from "../services/orderService";

import "./styles/Orders.css";

function PackageIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="products-icon"
    >
      <path d="M12 1 23 6.5v11L12 23 1 17.5v-11L12 1Z" />
      <path d="M1 6.5 12 12.5 23 6.5" />
      <path d="M12 12.5V23" />
    </svg>
  );
}



function ArrowIcon({ direction = "right" }) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="orders-icon"
    >
      {direction === "left" ? (
        <>
          <path d="M19 12H5" />
          <path d="m11 18-6-6 6-6" />
        </>
      ) : (
        <>
          <path d="M5 12h14" />
          <path d="m13 6 6 6-6 6" />
        </>
      )}
    </svg>
  );
}

function ClockIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="orders-icon"
    >
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7v5l3 2" />
    </svg>
  );
}

function CartIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="orders-icon"
    >
      <circle cx="9" cy="19" r="1.5" />
      <circle cx="18" cy="19" r="1.5" />
      <path d="M3.5 4.5h2l1.8 9.2h10.4l2-7H7" />
    </svg>
  );
}

function AlertIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="orders-icon"
    >
      <path d="M12 4 21 20H3L12 4Z" />
      <path d="M12 9v5" />
      <path d="M12 17h.01" />
    </svg>
  );
}

function Orders() {
  const {
    isAuthenticated,
    isLoading: isAuthLoading
  } = useAuth();

  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    let isMounted = true;

    async function loadOrders() {
      if (!isAuthenticated) {
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError("");

      try {
        const response = await getMyOrders({
          page,
          size: 10
        });

        if (!isMounted) {
          return;
        }

        setOrders(response?.content || []);
        setTotalPages(response?.totalPages || 0);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setError(
          requestError.message ||
            "Unable to load your orders."
        );
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    if (!isAuthLoading) {
      loadOrders();
    }

    return () => {
      isMounted = false;
    };
  }, [
    isAuthenticated,
    isAuthLoading,
    page
  ]);

  if (isAuthLoading) {
    return (
      <main className="page orders-page">
        <div className="container">
          <div className="orders-loading">
            <div className="orders-loading-icon">
              <PackageIcon />
            </div>

            <p>Loading your orders...</p>

            <div className="orders-loading-bar">
              <span />
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return (
      <main className="page orders-page">
        <div className="container">
          <div className="orders-state orders-auth-state">
            <div className="orders-state-icon">
              <PackageIcon />
            </div>

            <p className="orders-state-eyebrow">
              EcommerceHub
            </p>

            <h1 className="page-title">
              My Orders
            </h1>

            <p>
              Please sign in to view your orders.
            </p>

            <Link
              className="button orders-primary-button"
              to="/login"
            >
              <span>Sign In</span>
              <ArrowIcon />
            </Link>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="page orders-page">
      <div className="container">
        <header className="orders-header">
          <div className="orders-header-content">
            <div className="orders-title-row">
              <div className="orders-title-icon">
                <PackageIcon />
              </div>

              <div>
                <p className="orders-eyebrow">
                  EcommerceHub
                </p>

                <h1 className="page-title">
                  My Orders
                </h1>
              </div>
            </div>

            <p className="orders-description">
              View your previous orders, payment status,
              order details, and current progress.
            </p>

            <div className="orders-header-meta">
              <span className="orders-meta-item">
                <PackageIcon />
                Your order history
              </span>
            </div>
          </div>
        </header>

        {isLoading && (
          <div className="orders-loading">
            <div className="orders-loading-icon">
              <PackageIcon />
            </div>

            <p>Loading your orders...</p>

            <div className="orders-loading-bar">
              <span />
            </div>
          </div>
        )}

        {!isLoading && error && (
          <div
            className="orders-state orders-error-state"
            role="alert"
          >
            <div className="orders-state-icon orders-error-icon">
              <AlertIcon />
            </div>

            <p className="orders-state-eyebrow">
              Something went wrong
            </p>

            <p className="orders-error">
              {error}
            </p>
          </div>
        )}

        {!isLoading &&
          !error &&
          orders.length === 0 && (
            <div className="orders-state orders-empty-state">
              <div className="orders-state-icon">
                <PackageIcon />
              </div>

              <p className="orders-state-eyebrow">
                Order history
              </p>

              <h2>No orders yet</h2>

              <p>
                Your completed and active orders will
                appear here once you place an order.
              </p>

              <Link
                className="button orders-primary-button"
                to="/products"
              >
                <span>Start Shopping</span>
                <ArrowIcon />
              </Link>
            </div>
          )}

        {!isLoading &&
          !error &&
          orders.length > 0 && (
            <>
              <div className="orders-list">
                {orders.map((order, index) => {
                  const isPaymentPending =
                    order.status === "PENDING_PAYMENT";

                  return (
                    <article
                      className="order-card"
                      key={order.id}
                      style={{
                        "--order-delay": `${index * 70}ms`
                      }}
                    >
                      <div className="order-card-top">
                        <div className="order-card-identity">
                          <div className="order-card-icon">
                            <PackageIcon />
                          </div>

                          <div>
                            <p className="order-card-label">
                              Order
                            </p>

                            <h2>
                              #{order.id}
                            </h2>

                            {order.createdAt && (
                              <p className="order-card-date">
                                <ClockIcon />
                                {new Date(
                                  order.createdAt
                                ).toLocaleString()}
                              </p>
                            )}
                          </div>
                        </div>

                        <span
                          className={`order-status ${
                            isPaymentPending
                              ? "order-status-pending"
                              : ""
                          }`}
                        >
                          {order.status}
                        </span>
                      </div>

                      <div className="order-card-details">
                        <div className="order-detail-item">
                          <span className="order-detail-label">
                            <CartIcon />
                            Items
                          </span>

                          <strong>
                            {order.items?.reduce(
                              (total, item) => total + (item.quantity || 0),
                              0
                            )}
                          </strong>

                        </div>

                        <div className="order-detail-item order-total">
                          <span className="order-detail-label">
                            Total
                          </span>

                          <strong>
                            {order.currency}{" "}
                            {Number(
                              order.totalAmount
                            ).toFixed(2)}
                          </strong>
                        </div>
                      </div>

                      <div className="order-card-actions">
                        <Link
                          className="button order-view-button"
                          to={`/orders/${order.id}`}
                        >
                          <span>View Order</span>
                          <ArrowIcon />
                        </Link>

                        {isPaymentPending && (
                          <Link
                            className="button order-payment-button"
                            to={`/orders/${order.id}?payment=1`}
                          >
                            <span>Complete Payment</span>
                            <ArrowIcon />
                          </Link>
                        )}
                      </div>
                    </article>
                  );
                })}
              </div>

              {totalPages > 1 && (
                <nav
                  className="orders-pagination"
                  aria-label="Orders pagination"
                >
                  <button
                    type="button"
                    className="orders-pagination-button"
                    onClick={() =>
                      setPage((current) =>
                        Math.max(0, current - 1)
                      )
                    }
                    disabled={page === 0}
                  >
                    <ArrowIcon direction="left" />
                    <span>Previous</span>
                  </button>

                  <span className="orders-pagination-status">
                    Page {page + 1} of {totalPages}
                  </span>

                  <button
                    type="button"
                    className="orders-pagination-button"
                    onClick={() =>
                      setPage((current) =>
                        Math.min(
                          totalPages - 1,
                          current + 1
                        )
                      )
                    }
                    disabled={
                      page >= totalPages - 1
                    }
                  >
                    <span>Next</span>
                    <ArrowIcon />
                  </button>
                </nav>
              )}
            </>
          )}
      </div>
    </main>
  );
}

export default Orders;

