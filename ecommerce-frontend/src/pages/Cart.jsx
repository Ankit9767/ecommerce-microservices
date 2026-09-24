import React, { useState } from "react";
import { Link } from "react-router-dom";

import CartItem from "../components/CartItem";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";

import "./styles/Cart.css";

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
    emptyCart
  } = useCart();

  const { isAuthenticated } = useAuth();

  const [actionError, setActionError] =
    useState("");

  const handleQuantityChange = async (
    productId,
    quantity
  ) => {
    setActionError("");

    try {
      await updateQuantity(
        productId,
        quantity
      );
    } catch (requestError) {
      setActionError(
        requestError.message ||
          "Unable to update the cart."
      );
    }
  };

  const handleRemove = async (productId) => {
    setActionError("");

    try {
      await removeFromCart(productId);
    } catch (requestError) {
      setActionError(
        requestError.message ||
          "Unable to remove the item."
      );
    }
  };

  const handleClearCart = async () => {
    setActionError("");

    try {
      await emptyCart();
    } catch (requestError) {
      setActionError(
        requestError.message ||
          "Unable to clear the cart."
      );
    }
  };

  if (!isAuthenticated) {
    return (
      <section className="page cart-page">
        <div className="container">
          <div className="cart-empty">
            <h1 className="page-title">
              Your Cart
            </h1>

            <p>
              Please sign in to view your cart.
            </p>

            <Link
              className="button"
              to="/login"
            >
              Sign In
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
          <div className="cart-status">
            <p>Loading your cart...</p>
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
            className="cart-status cart-status-error"
            role="alert"
          >
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
          <div className="cart-empty">
            <h1 className="page-title">
              Your Cart
            </h1>

            <p>
              Your cart is currently empty.
            </p>

            <Link
              className="button"
              to="/products"
            >
              Browse Products
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
          <div>
            <p className="cart-eyebrow">
              EcommerceHub
            </p>

            <h1 className="page-title">
              Your Cart
            </h1>
          </div>

          <p className="cart-total-items">
            {totalItems}{" "}
            {totalItems === 1
              ? "item"
              : "items"}
          </p>
        </div>

        {(error || actionError) && (
          <div
            className="cart-status cart-status-error"
            role="alert"
          >
            <p>
              {actionError || error}
            </p>
          </div>
        )}

        <div className="cart-layout">
          <div className="cart-items">
            {items.map((item) => (
              <CartItem
                key={item.id}
                item={item}
                onQuantityChange={
                  handleQuantityChange
                }
                onRemove={handleRemove}
                disabled={isUpdating}
              />
            ))}
          </div>

          <aside className="cart-summary">
            <h2 className="cart-summary-title">
              Order Summary
            </h2>

            <div className="cart-summary-row">
              <span>Items</span>
              <span>{totalItems}</span>
            </div>

            <div className="cart-summary-row cart-summary-total">
              <span>Total</span>
              <span>
                ${Number(totalAmount).toFixed(2)}
              </span>
            </div>

            <button
              type="button"
              className="button cart-clear-button"
              onClick={handleClearCart}
              disabled={isUpdating}
            >
              Clear Cart
            </button>

            <Link
              className="cart-continue-link"
              to="/products"
            >
              Continue Shopping
            </Link>
          </aside>
        </div>
      </div>
    </section>
  );
}

export default Cart;