import React from "react";
import { Link } from "react-router-dom";

import CartItem from "../components/CartItem";
import { useCart } from "../context/CartContext";

import "./styles/Cart.css";

function Cart() {
  const {
    cartItems,
    cartItemCount,
    cartSubtotal,
    updateQuantity,
    removeFromCart,
    clearCart
  } = useCart();

  if (cartItems.length === 0) {
    return (
      <section className="cart-page">
        <div className="container">
          <div className="cart-empty">
            <p className="cart-eyebrow">
              Your shopping cart
            </p>

            <h1 className="page-title">
              Your Cart Is Empty
            </h1>

            <p className="cart-empty-description">
              Add some products to your cart and they will
              appear here.
            </p>

            <Link
              className="button"
              to="/products"
            >
              Continue Shopping
            </Link>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="cart-page">
      <div className="container">
        <div className="cart-header">
          <div>
            <p className="cart-eyebrow">
              Your shopping cart
            </p>

            <h1 className="page-title">
              Cart
            </h1>

            <p className="cart-item-count">
              {cartItemCount}{" "}
              {cartItemCount === 1
                ? "item"
                : "items"}
            </p>
          </div>

          <button
            type="button"
            className="cart-clear-button"
            onClick={clearCart}
          >
            Clear Cart
          </button>
        </div>

        <div className="cart-layout">
          <div className="cart-items">
            {cartItems.map((item) => (
              <CartItem
                key={item.id}
                item={item}
                onQuantityChange={updateQuantity}
                onRemove={removeFromCart}
              />
            ))}
          </div>

          <aside className="cart-summary">
            <h2 className="cart-summary-title">
              Order Summary
            </h2>

            <div className="cart-summary-row">
              <span>
                Subtotal
              </span>

              <strong>
                ${cartSubtotal.toFixed(2)}
              </strong>
            </div>

            <div className="cart-summary-row">
              <span>
                Shipping
              </span>

              <span>
                Calculated at checkout
              </span>
            </div>

            <div className="cart-summary-divider" />

            <div className="cart-summary-total">
              <span>
                Total
              </span>

              <strong>
                ${cartSubtotal.toFixed(2)}
              </strong>
            </div>

            <button
              type="button"
              className="button cart-checkout-button"
            >
              Proceed to Checkout
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