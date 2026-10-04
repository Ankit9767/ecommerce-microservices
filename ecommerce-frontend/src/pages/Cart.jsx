import React, { useState } from "react";
import { Link } from "react-router-dom";

import CartItem from "../components/CartItem";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";

import "./styles/Cart.css";

function CartIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="cart-icon">
      <path d="M4 5h2l1.4 9.2a2 2 0 0 0 2 1.7h7.9a2 2 0 0 0 1.9-1.5L21 8H7" />
      <circle cx="10" cy="19" r="1.2" />
      <circle cx="18" cy="19" r="1.2" />
    </svg>
  );
}

function MoneyIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="cart-summary-total-icon"
    >
      <rect x="3.5" y="6" width="17" height="12" rx="2" />
      <path d="M3.5 9h17" />
      <circle cx="12" cy="13.5" r="2" />
      <path d="M7 13.5h.01" />
      <path d="M17 13.5h.01" />
    </svg>
  );
}

function ProductsIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="cart-icon">
      <rect x="4" y="4" width="16" height="16" rx="2" />
      <path d="M4 9h16" />
      <path d="M9 9v11" />
    </svg>
  );
}

function SummaryIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="cart-icon">
      <path d="M6 3.5h12a1.5 1.5 0 0 1 1.5 1.5v14a1.5 1.5 0 0 1-1.5 1.5H6A1.5 1.5 0 0 1 4.5 19V5A1.5 1.5 0 0 1 6 3.5Z" />
      <path d="M8 8h8" />
      <path d="M8 12h8" />
      <path d="M8 16h5" />
    </svg>
  );
}

function TrashIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="cart-button-icon">
      <path d="M5 7h14" />
      <path d="M9 7V4.5h6V7" />
      <path d="m7 7 .8 12h8.4L17 7" />
      <path d="M10 11v5" />
      <path d="M14 11v5" />
    </svg>
  );
}

function CheckoutIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="cart-button-icon">
      <path d="M4 5h2l1.4 9.2a2 2 0 0 0 2 1.7h7.9a2 2 0 0 0 1.9-1.5L21 8H7" />
      <path d="M14 12h5" />
      <path d="m17 9 3 3-3 3" />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="cart-button-icon">
      <path d="M19 12H5" />
      <path d="m11 6-6 6 6 6" />
    </svg>
  );
}

function AlertIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="cart-alert-icon">
      <path d="M12 4 21 19H3L12 4Z" />
      <path d="M12 9v5" />
      <path d="M12 17.2v.1" />
    </svg>
  );
}

function Cart() {
  const {
    items,
    totalAmount,
    totalItems,
    isLoading,
    isUpdating,
    error,
    updateQuantity,
    removeFromCart,
    emptyCart,
  } = useCart();

  const { isAuthenticated } = useAuth();

  const [actionError, setActionError] = useState("");

  const handleQuantityChange = async (productId, quantity) => {
    setActionError("");

    try {
      await updateQuantity(productId, quantity);
    } catch (requestError) {
      setActionError(requestError.message || "Unable to update the cart.");
    }
  };

  const handleRemove = async (productId) => {
    setActionError("");

    try {
      await removeFromCart(productId);
    } catch (requestError) {
      setActionError(requestError.message || "Unable to remove the item.");
    }
  };

  const handleClearCart = async () => {
    setActionError("");

    try {
      await emptyCart();
    } catch (requestError) {
      setActionError(requestError.message || "Unable to clear the cart.");
    }
  };

  if (!isAuthenticated) {
    return (
      <section className="page cart-page">
        <div className="container">
          <div className="cart-empty cart-state-card">
            <div className="cart-state-icon">
              <CartIcon />
            </div>

            <h1 className="page-title">Your Cart</h1>

            <p>Please sign in to view your cart.</p>

            <Link className="button cart-state-button" to="/login">
              <span>Sign In</span>
            </Link>
          </div>
        </div>
      </section>
    );
  }

  if (isLoading) {
    return (
      <section className="page cart-page">
        <div className="container">
          <div className="cart-status cart-state-card">
            <div className="cart-loading-icon">
              <CartIcon />
            </div>

            <p>Loading your cart...</p>

            <span className="cart-loading-spinner" aria-hidden="true" />
          </div>
        </div>
      </section>
    );
  }

  if (error && items.length === 0) {
    return (
      <section className="page cart-page">
        <div className="container">
          <div
            className="cart-status cart-status-error cart-state-card"
            role="alert"
          >
            <div className="cart-state-icon cart-state-icon-error">
              <AlertIcon />
            </div>

            <h1 className="page-title">Something went wrong</h1>

            <p>{error}</p>
          </div>
        </div>
      </section>
    );
  }

  if (items.length === 0) {
    return (
      <section className="page cart-page">
        <div className="container">
          <div className="cart-empty cart-state-card">
            <div className="cart-state-icon">
              <CartIcon />
            </div>

            <h1 className="page-title">Your Cart</h1>

            <p>Your cart is currently empty.</p>

            <Link className="button cart-state-button" to="/products">
              <ProductsIcon />

              <span>Browse Products</span>
            </Link>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="page cart-page">
      <div className="container">
        <div className="cart-header">
          <div className="cart-header-content">
            <div className="cart-heading-icon">
              <CartIcon />
            </div>

            <div>
              <p className="cart-eyebrow">EcommerceHub</p>

              <h1 className="page-title">Your Cart</h1>
            </div>
          </div>
        </div>

        {(error || actionError) && (
          <div className="cart-status cart-status-error" role="alert">
            <div className="cart-alert-icon-wrap">
              <AlertIcon />
            </div>

            <p>{actionError || error}</p>
          </div>
        )}

        <div className="cart-layout">
          <div className="cart-items">
            {items.map((item, index) => (
              <div
                className="cart-item-wrapper"
                key={item.id}
                style={{
                  "--cart-item-delay": `${index * 70}ms`,
                }}
              >
                <CartItem
                  item={item}
                  onQuantityChange={handleQuantityChange}
                  onRemove={handleRemove}
                  disabled={isUpdating}
                />
              </div>
            ))}
          </div>

          <aside className="cart-summary">
            <div className="cart-summary-glow" />

            <div className="cart-summary-heading">
              <div className="cart-summary-icon">
                <SummaryIcon />
              </div>

              <div>
                <p className="cart-summary-eyebrow">Your Order</p>

                <h2 className="cart-summary-title">Order Summary</h2>
              </div>
            </div>

            <div className="cart-summary-details">
              <div className="cart-summary-row">
                <div className="cart-summary-label">
                  <ProductsIcon />

                  <span>Items</span>
                </div>

                <strong>{totalItems}</strong>
              </div>
            </div>

            <div className="cart-summary-total-card">
              <div className="cart-summary-total-info">
                <div className="cart-summary-total-heading">
                  <MoneyIcon />

                  <span className="cart-summary-total-label">Total amount</span>
                </div>

                <span className="cart-summary-total-note">
                  Including all items
                </span>
              </div>

              <strong>{Number(totalAmount).toFixed(2)}</strong>
            </div>

            <div className="cart-summary-actions">
              <Link className="button cart-checkout-button" to="/checkout">
                <CheckoutIcon />

                <span>Proceed to Checkout</span>
              </Link>

              <button
                type="button"
                className="cart-clear-button"
                onClick={handleClearCart}
                disabled={isUpdating}
              >
                <TrashIcon />

                <span>{isUpdating ? "Updating..." : "Clear Cart"}</span>
              </button>
            </div>

            <Link className="cart-continue-link" to="/products">
              <ArrowIcon />

              <span>Continue Shopping</span>
            </Link>

            <div className="cart-summary-footer">
              <span className="cart-summary-footer-dot" />

              <span>Ready for checkout</span>
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
}

export default Cart;