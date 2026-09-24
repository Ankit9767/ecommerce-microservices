import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import QuantitySelector from "../components/QuantitySelector";
import { useCart } from "../context/CartContext";
import { getProduct } from "../services/productService";

import "./styles/ProductDetails.css";

function ProductDetails() {
  const { id } = useParams();
  const { addToCart } = useCart();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [addedToCart, setAddedToCart] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadProduct() {
      setIsLoading(true);
      setError("");
      setProduct(null);
      setQuantity(1);
      setAddedToCart(false);

      try {
        const response = await getProduct(id);

        if (!isMounted) {
          return;
        }

        setProduct(response);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        if (requestError.status === 404) {
          setError("Product not found.");
        } else {
          setError(
            requestError.message ||
              "Unable to load product. Please try again."
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    if (!id) {
      setIsLoading(false);
      setError("Product ID is missing.");

      return () => {
        isMounted = false;
      };
    }

    loadProduct();

    return () => {
      isMounted = false;
    };
  }, [id]);

  const handleQuantityChange = (nextQuantity) => {
    setQuantity(nextQuantity);
    setAddedToCart(false);
  };

  const handleAddToCart = () => {
    if (!product || quantity < 1) {
      return;
    }

    addToCart(product, quantity);
    setAddedToCart(true);
  };

  if (isLoading) {
    return (
      <section className="page product-details-page">
        <div className="container">
          <div className="product-details-status">
            <p>Loading product...</p>
          </div>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="page product-details-page">
        <div className="container">
          <div
            className="product-details-status product-details-status-error"
            role="alert"
          >
            <p>{error}</p>

            <Link
              className="button product-details-back-button"
              to="/products"
            >
              Back to Products
            </Link>
          </div>
        </div>
      </section>
    );
  }

  if (!product) {
    return (
      <section className="page product-details-page">
        <div className="container">
          <div className="product-details-status">
            <p>Product not found.</p>

            <Link
              className="button product-details-back-button"
              to="/products"
            >
              Back to Products
            </Link>
          </div>
        </div>
      </section>
    );
  }

  const price = Number(product.price);

  return (
    <section className="page product-details-page">
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
              aria-label={`${product.name} image placeholder`}
            >
              {product.name
                ? product.name.charAt(0).toUpperCase()
                : "P"}
            </div>
          </div>

          <div className="product-details-content">
            {product.category && (
              <p className="product-details-category">
                {product.category}
              </p>
            )}

            <h1 className="product-details-title">
              {product.name}
            </h1>

            {product.sku && (
              <p className="product-details-sku">
                SKU: {product.sku}
              </p>
            )}

            <p className="product-details-price">
              {Number.isFinite(price)
                ? `$${price.toFixed(2)}`
                : "Price unavailable"}
            </p>

            {product.description && (
              <div className="product-details-description">
                <h2>Description</h2>
                <p>{product.description}</p>
              </div>
            )}

            {product.active === false && (
              <p className="product-details-unavailable">
                This product is currently unavailable.
              </p>
            )}

            {product.active !== false && (
              <div className="product-details-purchase">
                <QuantitySelector
                  quantity={quantity}
                  onQuantityChange={handleQuantityChange}
                />

                <button
                  type="button"
                  className="button product-details-cart-button"
                  onClick={handleAddToCart}
                >
                  Add to Cart
                </button>

                {addedToCart && (
                  <p
                    className="product-details-cart-success"
                    role="status"
                  >
                    Product added to cart.
                  </p>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

export default ProductDetails;