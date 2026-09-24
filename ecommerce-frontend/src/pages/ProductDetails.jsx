import React, { useState } from "react";
import { Link, useParams } from "react-router-dom";

import QuantitySelector from "../components/QuantitySelector";

import "./styles/ProductDetails.css";

const products = [
  {
    id: "product-1",
    name: "Wireless Headphones",
    price: 79.99,
    category: "Electronics",
    rating: 4.8,
    ratingCount: 124,
    description:
      "Enjoy clear sound, comfortable ear cushions, and reliable wireless connectivity for everyday listening."
  },
  {
    id: "product-2",
    name: "Classic Sneakers",
    price: 64.99,
    category: "Fashion",
    rating: 4.6,
    ratingCount: 89,
    description:
      "A versatile everyday sneaker designed for comfort, casual styling, and all-day wear."
  },
  {
    id: "product-3",
    name: "Smart Watch",
    price: 129.99,
    category: "Electronics",
    rating: 4.7,
    ratingCount: 216,
    description:
      "Stay connected throughout the day with useful smart features, notifications, and activity tracking."
  },
  {
    id: "product-4",
    name: "Minimal Desk Lamp",
    price: 39.99,
    category: "Home & Living",
    rating: 4.5,
    ratingCount: 67,
    description:
      "A clean and minimal desk lamp that provides comfortable lighting for work and study."
  }
];

function ProductDetails() {
  const { id } = useParams();

  const [quantity, setQuantity] = useState(1);
  const [addedToCart, setAddedToCart] = useState(false);

  const product = products.find(
    (item) => item.id === id
  );

  if (!product) {
    return (
      <section className="page">
        <div className="container">
          <h1 className="page-title">
            Product Not Found
          </h1>

          <p className="page-description">
            We couldn't find the product you're looking for.
          </p>

          <Link
            className="button"
            to="/products"
          >
            Back to Products
          </Link>
        </div>
      </section>
    );
  }

  const formattedPrice = `$${product.price.toFixed(2)}`;

  const handleAddToCart = () => {
    setAddedToCart(true);
  };

  return (
    <section className="product-details-page">
      <div className="container">
        <Link
          className="product-details-back-link"
          to="/products"
        >
          ← Back to Products
        </Link>

        <div className="product-details">
          <div className="product-details-image">
            <div
              className="product-details-image-placeholder"
              aria-label={product.name}
            >
              {product.name.charAt(0)}
            </div>
          </div>

          <div className="product-details-info">
            <p className="product-details-category">
              {product.category}
            </p>

            <h1 className="product-details-title">
              {product.name}
            </h1>

            <div className="product-details-rating">
              <span className="product-details-rating-value">
                ★ {product.rating}
              </span>

              <span className="product-details-rating-count">
                ({product.ratingCount} reviews)
              </span>
            </div>

            <p className="product-details-price">
              {formattedPrice}
            </p>

            <p className="product-details-description">
              {product.description}
            </p>

            <div className="product-details-purchase">
              <span className="product-details-quantity-label">
                Quantity
              </span>

              <QuantitySelector
                value={quantity}
                onChange={setQuantity}
              />

              <div className="product-details-actions">
                <button
                  type="button"
                  className="button product-details-cart-button"
                  onClick={handleAddToCart}
                >
                  Add to Cart
                </button>

                <button
                  type="button"
                  className="product-details-buy-button"
                >
                  Buy Now
                </button>
              </div>

              {addedToCart && (
                <p
                  className="product-details-status"
                  role="status"
                >
                  {quantity} item
                  {quantity > 1 ? "s" : ""} added to your cart.
                </p>
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default ProductDetails;