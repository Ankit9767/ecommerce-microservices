import React from "react";

import "./styles/QuantitySelector.css";

function QuantitySelector({
  quantity,
  onQuantityChange,
  min = 1,
  max = 99,
  disabled = false
}) {
  const handleDecrease = () => {
    if (disabled || quantity <= min) {
      return;
    }

    onQuantityChange(quantity - 1);
  };

  const handleIncrease = () => {
    if (disabled || quantity >= max) {
      return;
    }

    onQuantityChange(quantity + 1);
  };

  return (
    <div className="quantity-selector">
      <button
        type="button"
        className="quantity-selector-button"
        onClick={handleDecrease}
        disabled={disabled || quantity <= min}
        aria-label="Decrease quantity"
      >
        −
      </button>

      <span className="quantity-selector-value">
        {quantity}
      </span>

      <button
        type="button"
        className="quantity-selector-button"
        onClick={handleIncrease}
        disabled={disabled || quantity >= max}
        aria-label="Increase quantity"
      >
        +
      </button>
    </div>
  );
}

export default QuantitySelector;