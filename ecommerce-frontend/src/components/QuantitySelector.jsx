import React from "react";

import "./styles/QuantitySelector.css";

function QuantitySelector({
  value,
  onChange,
  min = 1,
  max = 99
}) {
  const decrease = () => {
    if (value > min) {
      onChange(value - 1);
    }
  };

  const increase = () => {
    if (value < max) {
      onChange(value + 1);
    }
  };

  return (
    <div className="quantity-selector">
      <button
        type="button"
        className="quantity-button"
        onClick={decrease}
        disabled={value <= min}
        aria-label="Decrease quantity"
      >
        −
      </button>

      <span
        className="quantity-value"
        aria-live="polite"
      >
        {value}
      </span>

      <button
        type="button"
        className="quantity-button"
        onClick={increase}
        disabled={value >= max}
        aria-label="Increase quantity"
      >
        +
      </button>
    </div>
  );
}

export default QuantitySelector;