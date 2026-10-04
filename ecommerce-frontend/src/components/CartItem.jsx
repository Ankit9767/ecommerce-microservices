import React from "react";

import QuantitySelector from "./QuantitySelector";

import "./styles/CartItem.css";

function ProductsIcon() {
  return (
    <svg viewBox="2 2 20 20" aria-hidden="true" className="nav-icon">
      <rect x="3" y="3" width="18" height="18" rx="2" />
      <path d="M3 9h18" />
      <path d="M9 9v12" />
    </svg>
  );
}

function PriceIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="cart-item-meta-icon">
      <path d="M4 5.5h10l6 6-8.5 8.5-6-6V5.5Z" />
      <circle cx="8.5" cy="9" r="1.1" />
    </svg>
  );
}

function TrashIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="cart-item-remove-icon"
    >
      <path d="M5 7h14" />
      <path d="M9 7V4.5h6V7" />
      <path d="m7 7 .8 12h8.4L17 7" />
      <path d="M10 11v5" />
      <path d="M14 11v5" />
    </svg>
  );
}

function CartItem({ item, onQuantityChange, onRemove, disabled = false }) {
  const unitPrice = Number(item.unitPrice);
  const lineTotal = Number(item.lineTotal);

  return (
    <article className="cart-item">
      <div className="cart-item-content">
        <div className="cart-item-product">
          <div className="cart-item-product-icon">
            <ProductsIcon />
          </div>

          <div className="cart-item-details">
            <p className="cart-item-eyebrow">Product</p>

            <h2 className="cart-item-name">{item.productName}</h2>

            {item.sku && <p className="cart-item-sku">SKU: {item.sku}</p>}

            <div className="cart-item-unit-price">
              <PriceIcon />
              <span>{unitPrice.toFixed(2)} each</span>
            </div>
          </div>
        </div>

        <div className="cart-item-actions">
          <div className="cart-item-quantity">
            <span className="cart-item-action-label">Quantity</span>

            <QuantitySelector
              quantity={item.quantity}
              onQuantityChange={(quantity) =>
                onQuantityChange(item.productId, quantity)
              }
              disabled={disabled}
            />
          </div>

          <div className="cart-item-line-total">
            <span className="cart-item-action-label">Total</span>

            <div className="cart-item-total-value">
              <strong>{lineTotal.toFixed(2)}</strong>
            </div>
          </div>

          <button
            type="button"
            className="cart-item-remove"
            onClick={() => onRemove(item.productId)}
            disabled={disabled}
            aria-label={`Remove ${item.productName} from cart`}
          >
            <TrashIcon />
            <span>Remove</span>
          </button>
        </div>
      </div>
    </article>
  );
}

export default CartItem;