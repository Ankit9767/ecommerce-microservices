import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import { getMyOrders } from "../services/orderService";

import "./styles/Orders.css";

function Orders() {
  const { isAuthenticated, isLoading: isAuthLoading } = useAuth();

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
  }, [isAuthenticated, isAuthLoading, page]);

  if (isAuthLoading) {
    return (
      <main className="page">
        <div className="container">
          <p className="orders-state">Loading...</p>
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return (
      <main className="page">
        <div className="container">
          <div className="orders-state">
            <h1 className="page-title">My Orders</h1>
            <p>Please sign in to view your orders.</p>
            <Link className="button" to="/login">
              Sign In
            </Link>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="page">
      <div className="container">
        <div className="orders-header">
          <div>
            <h1 className="page-title">My Orders</h1>
            <p className="page-description">
              View your previous orders and their current status.
            </p>
          </div>
        </div>

        {isLoading && (
          <p className="orders-state">
            Loading your orders...
          </p>
        )}

        {!isLoading && error && (
          <div className="orders-state">
            <p className="orders-error">{error}</p>
          </div>
        )}

        {!isLoading && !error && orders.length === 0 && (
          <div className="orders-state">
            <p>You have no orders yet.</p>
            <Link className="button" to="/products">
              Start Shopping
            </Link>
          </div>
        )}

        {!isLoading && !error && orders.length > 0 && (
          <>
            <div className="orders-list">
              {orders.map((order) => (
                <article
                  className="order-card"
                  key={order.id}
                >
                  <div className="order-card-header">
                    <div>
                      <h2>
                        Order #{order.id}
                      </h2>
                      <p>
                        {order.createdAt
                          ? new Date(
                              order.createdAt
                            ).toLocaleString()
                          : ""}
                      </p>
                    </div>

                    <span className="order-status">
                      {order.status}
                    </span>
                  </div>

                  <div className="order-card-details">
                    <span>
                      Items: {order.items?.length || 0}
                    </span>

                    <strong>
                      {order.currency}{" "}
                      {Number(
                        order.totalAmount
                      ).toFixed(2)}
                    </strong>
                  </div>

                  <Link
                    className="button order-view-button"
                    to={`/orders/${order.id}`}
                  >
                    View Order
                  </Link>
                </article>
              ))}
            </div>

            {totalPages > 1 && (
              <div className="orders-pagination">
                <button
                  type="button"
                  className="button"
                  onClick={() =>
                    setPage((current) =>
                      Math.max(0, current - 1)
                    )
                  }
                  disabled={page === 0}
                >
                  Previous
                </button>

                <span>
                  Page {page + 1} of {totalPages}
                </span>

                <button
                  type="button"
                  className="button"
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
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </main>
  );
}

export default Orders;