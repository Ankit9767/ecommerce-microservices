import React from "react";
import { Link } from "react-router-dom";

import "./styles/ProductCard.css";

function ProductCard({ product }) {
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
          <span>{product.name.charAt(0)}</span>
        )}
      </Link>

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
            ${product.price.toFixed(2)}
          </span>

          {product.rating && (
            <span className="product-card-rating">
              ★ {product.rating}
            </span>
          )}
        </div>
      </div>
    </article>
  );
}

export default ProductCard;