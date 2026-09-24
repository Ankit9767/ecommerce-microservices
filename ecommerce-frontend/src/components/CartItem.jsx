import React from "react";

import QuantitySelector from "./QuantitySelector";

import "./styles/CartItem.css";

function CartItem({
  item,
  onQuantityChange,
  onRemove,
  disabled = false
}) {
  const unitPrice = Number(item.unitPrice);
  const lineTotal = Number(item.lineTotal);

  return (
    <article className="cart-item">
      <div className="cart-item-content">
        <div className="cart-item-details">
          <h2 className="cart-item-name">
            {item.productName}
          </h2>

          {item.sku && (
            <p className="cart-item-sku">
              SKU: {item.sku}
            </p>
          )}

          <p className="cart-item-unit-price">
            ${unitPrice.toFixed(2)} each
          </p>
        </div>

        <div className="cart-item-actions">
          <QuantitySelector
            quantity={item.quantity}
            onQuantityChange={(quantity) =>
              onQuantityChange(
                item.productId,
                quantity
              )
            }
            disabled={disabled}
          />

          <p className="cart-item-line-total">
            ${lineTotal.toFixed(2)}
          </p>

          <button
            type="button"
            className="cart-item-remove"
            onClick={() =>
              onRemove(item.productId)
            }
            disabled={disabled}
          >
            Remove
          </button>
        </div>
      </div>
    </article>
  );
}

export default CartItem;