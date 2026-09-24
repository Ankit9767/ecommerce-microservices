import React from "react";

import QuantitySelector from "./QuantitySelector";

import "./styles/CartItem.css";

function CartItem({
  item,
  onQuantityChange,
  onRemove
}) {
  const itemTotal = item.price * item.quantity;

  return (
    <article className="cart-item">
      <div className="cart-item-image">
        {item.image ? (
          <img
            src={item.image}
            alt={item.name}
          />
        ) : (
          <span>
            {item.name.charAt(0)}
          </span>
        )}
      </div>

      <div className="cart-item-content">
        <div className="cart-item-details">
          <p className="cart-item-category">
            {item.category}
          </p>

          <h2 className="cart-item-name">
            {item.name}
          </h2>

          <p className="cart-item-price">
            ${item.price.toFixed(2)}
          </p>
        </div>

        <div className="cart-item-actions">
          <QuantitySelector
            value={item.quantity}
            onChange={(quantity) =>
              onQuantityChange(item.id, quantity)
            }
          />

          <p className="cart-item-total">
            ${itemTotal.toFixed(2)}
          </p>

          <button
            type="button"
            className="cart-item-remove"
            onClick={() => onRemove(item.id)}
          >
            Remove
          </button>
        </div>
      </div>
    </article>
  );
}

export default CartItem;