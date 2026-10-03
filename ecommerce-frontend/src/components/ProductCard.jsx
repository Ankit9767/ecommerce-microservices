import React, { useMemo, useState } from "react";

import {
Link,
useNavigate
} from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

import QuantitySelector from "./QuantitySelector";

import "./styles/ProductCard.css";

function getCartItemProductId(item) {
return (
item?.productId ||
item?.product?.id ||
item?.product?.productId ||
null
);
}

function getCartItemQuantity(item) {
return Number(
item?.quantity ||
item?.productQuantity ||
0
);
}

function ProductCard({ product }) {
const navigate = useNavigate();

const {
isAuthenticated
} = useAuth();

const {
  items,
  addToCart,
  updateQuantity,
  removeFromCart,
  isUpdating,
  addingProductId
} = useCart();


const [error, setError] = useState("");
const [isPressed, setIsPressed] = useState(false);
const [isRemoving, setIsRemoving] = useState(false);

const cartItem = useMemo(() => {
if (!product?.id || !Array.isArray(items)) {
return null;
}

return (
  items.find(
    (item) =>
      String(getCartItemProductId(item)) ===
      String(product.id)
  ) || null
);


}, [items, product?.id]);

const cartQuantity = cartItem
? getCartItemQuantity(cartItem)
: 0;

const isInCart = Boolean(cartItem);

const isAdding =
  String(addingProductId) === String(product.id);


const handleAddToCart = async () => {
setError("");
setIsPressed(true);


window.setTimeout(() => {
  setIsPressed(false);
}, 220);

if (!isAuthenticated) {
  navigate("/login", {
    state: {
      from: `/products/${product.id}`,
      message:
        "Please sign in to add products to your cart."
    }
  });

  return;
}

try {
  await addToCart(product, 1);
} catch (requestError) {
  setError(
    requestError.message ||
      "Unable to add this product to your cart."
  );
}


};

const handleQuantityChange = async (quantity) => {
if (!product?.id || quantity < 1) {
return;
}

setError("");

try {
  await updateQuantity(
    product.id,
    quantity
  );
} catch (requestError) {
  setError(
    requestError.message ||
      "Unable to update this product quantity."
  );
}

};

const handleRemoveFromCart = async () => {
if (!product?.id || isRemoving) {
return;
}


setError("");
setIsRemoving(true);

try {
  await removeFromCart(product.id);
} catch (requestError) {
  setError(
    requestError.message ||
      "Unable to remove this product from your cart."
  );
} finally {
  setIsRemoving(false);
}


};

return (
<article
className={`product-card${
        isPressed ? " product-card--pressed" : ""
      }`}
>
<Link
className="product-card-image"
to={`/products/${product.id}`}
aria-label={`View ${product.name}`}
>
{product.image ? ( <img
         src={product.image}
         alt={product.name}
       />
) : ( <span>
{product.name?.charAt(0)} </span>
)} </Link>

  <div className="product-card-content">
    <p className="product-card-category">
      {product.category}
    </p>

    <h2 className="product-card-name">
      <Link to={`/products/${product.id}`}>
        {product.name}
      </Link>
    </h2>

    <div className="product-card-footer">
      <span className="product-card-price">
        {product.currency || "USD"}{" "}
        {Number(product.price).toFixed(2)}
      </span>

      {product.rating !== null &&
        product.rating !== undefined && (
          <span className="product-card-rating">
            <span aria-hidden="true">★</span>{" "}
            {Number(product.rating).toFixed(1)}
          </span>
        )}
    </div>

    <div
      className={`product-card-action${
        isInCart
          ? " product-card-action--quantity"
          : ""
      }`}
    >
      {isInCart ? (
        <div className="product-card-cart-controls">
          <QuantitySelector
            quantity={cartQuantity}
            onQuantityChange={handleQuantityChange}
            min={1}
            max={99}
            disabled={isUpdating || isRemoving}
          />

          <button
            type="button"
            className="product-card-remove-button"
            onClick={handleRemoveFromCart}
            disabled={isRemoving}
            aria-label={`Remove ${product.name} from cart`}
            title="Remove from cart"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M4 7h16" />
              <path d="M9 7V4h6v3" />
              <path d="M7 7l1 13h8l1-13" />
              <path d="M10 11v5" />
              <path d="M14 11v5" />
            </svg>
          </button>
        </div>
      ) : (
        <button
          type="button"
          className="product-card-cart-button"
          onClick={handleAddToCart}
          disabled={isAdding}
        >
          <span className="product-card-cart-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M6 7h12l1 13H5L6 7Z" />
              <path d="M9 7a3 3 0 0 1 6 0" />
            </svg>
          </span>

          <span>
            {isAdding
              ? "Adding..."
              : "Add to Cart"}
          </span>
        </button>
      )}
    </div>

    {error && (
      <p
        className="product-card-error"
        role="alert"
      >
        {error}
      </p>
    )}
  </div>
</article>

);
}

export default ProductCard;
