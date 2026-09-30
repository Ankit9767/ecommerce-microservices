import React, {
  useState
} from "react";

import {
  Link,
  useNavigate
} from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

import "./styles/ProductCard.css";

function ProductCard({ product }) {
  const navigate = useNavigate();

  const {
    isAuthenticated
  } = useAuth();

  const {
    addToCart,
    isUpdating
  } = useCart();

  const [error, setError] =
    useState("");

  const handleAddToCart = async () => {
    setError("");

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

  return (
    <article className="product-card">
      <Link
        className="product-card-image"
        to={`/products/${product.id}`}
        aria-label={`View ${product.name}`}
      >
        {product.image ? (
          <img
            src={product.image}
            alt={product.name}
          />
        ) : (
          <span>
            {product.name.charAt(0)}
          </span>
        )}
      </Link>

      <div className="product-card-content">
        <p className="product-card-category">
          {product.category}
        </p>

        <h2 className="product-card-name">
          <Link
            to={`/products/${product.id}`}
          >
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
                ★{" "}
                {Number(
                  product.rating
                ).toFixed(1)}
              </span>
            )}
        </div>

        <button
          type="button"
          className="product-card-cart-button"
          onClick={handleAddToCart}
          disabled={isUpdating}
        >
          {isUpdating
            ? "Adding..."
            : "Add to Cart"}
        </button>

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