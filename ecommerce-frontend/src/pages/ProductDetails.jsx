import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import QuantitySelector from "../components/QuantitySelector";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { getProduct } from "../services/productService";

import ReviewCard from "../components/ReviewCard";

import {
  getProductReviews
} from "../services/reviewService";

import "./styles/ProductDetails.css";

function ProductDetails() {
  const { id } = useParams();

  const { isAuthenticated } = useAuth();
  const { addToCart } = useCart();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);

  const [isLoading, setIsLoading] = useState(true);
  const [isAddingToCart, setIsAddingToCart] =
    useState(false);

  const [error, setError] = useState("");
  const [cartError, setCartError] = useState("");
  const [addedToCart, setAddedToCart] = useState(false);

  const [reviews, setReviews] = useState([]);
  const [isReviewsLoading, setIsReviewsLoading] =
    useState(false);
  const [reviewsError, setReviewsError] =
    useState("");
  const [reviewTotalElements, setReviewTotalElements] =
    useState(0);

  // Load product
  useEffect(() => {
    let isMounted = true;

    async function loadProduct() {
      if (!id) {
        setIsLoading(false);
        setError("Product ID is missing.");
        return;
      }

      setIsLoading(true);
      setError("");
      setCartError("");
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

    loadProduct();

    return () => {
      isMounted = false;
    };
  }, [id]);

  // Load reviews
  useEffect(() => {
    let isMounted = true;

    async function loadReviews() {
      if (!id) {
        return;
      }

      setIsReviewsLoading(true);
      setReviewsError("");

      try {
        const response =
          await getProductReviews(id, {
            page: 0,
            size: 20,
            sort: "createdAt,desc"
          });

        if (!isMounted) {
          return;
        }

        setReviews(response?.content || []);
        setReviewTotalElements(
          Number(response?.totalElements || 0)
        );
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setReviewsError(
          requestError.message ||
            "Unable to load product reviews."
        );
      } finally {
        if (isMounted) {
          setIsReviewsLoading(false);
        }
      }
    }

    loadReviews();

    return () => {
      isMounted = false;
    };
  }, [id]);

  const handleQuantityChange = (nextQuantity) => {
    setQuantity(nextQuantity);
    setAddedToCart(false);
    setCartError("");
  };

  const handleAddToCart = async () => {
    if (
      !product ||
      quantity < 1 ||
      !isAuthenticated ||
      isAddingToCart
    ) {
      return;
    }

    setIsAddingToCart(true);
    setAddedToCart(false);
    setCartError("");

    try {
      await addToCart(product, quantity);

      setAddedToCart(true);
    } catch (requestError) {
      setCartError(
        requestError.message ||
          "Unable to add the product to your cart."
      );
    } finally {
      setIsAddingToCart(false);
    }
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
                  onQuantityChange={
                    handleQuantityChange
                  }
                  disabled={isAddingToCart}
                />

                {isAuthenticated ? (
                  <button
                    type="button"
                    className="button product-details-cart-button"
                    onClick={handleAddToCart}
                    disabled={isAddingToCart}
                  >
                    {isAddingToCart
                      ? "Adding..."
                      : "Add to Cart"}
                  </button>
                ) : (
                  <Link
                    className="button product-details-cart-button"
                    to="/login"
                  >
                    Sign In to Add to Cart
                  </Link>
                )}

                {addedToCart && (
                  <p
                    className="product-details-cart-success"
                    role="status"
                  >
                    Product added to cart.
                  </p>
                )}

                {cartError && (
                  <p
                    className="product-details-cart-error"
                    role="alert"
                  >
                    {cartError}
                  </p>
                )}
              </div>
            )}
          </div>
        </div>
        <section className="product-reviews-section">
  <div className="product-reviews-header">
    <div>
      <p className="product-reviews-eyebrow">
        Customer Reviews
      </p>

      <h2>
        Reviews
        {reviewTotalElements > 0 && (
          <span className="product-reviews-count">
            {reviewTotalElements}
          </span>
        )}
      </h2>
    </div>
  </div>

  {isReviewsLoading && (
    <div
      className="product-reviews-state"
      aria-live="polite"
    >
      Loading reviews...
    </div>
  )}

  {!isReviewsLoading && reviewsError && (
    <div className="product-reviews-state">
      <p
        className="product-reviews-error"
        role="alert"
      >
        {reviewsError}
      </p>
    </div>
  )}

  {!isReviewsLoading &&
    !reviewsError &&
    reviews.length === 0 && (
      <div className="product-reviews-state">
        <p>
          No reviews yet. Be the first customer to
          review this product after your purchase.
        </p>
      </div>
    )}

  {!isReviewsLoading &&
    !reviewsError &&
    reviews.length > 0 && (
      <div className="product-reviews-list">
        {reviews.map((review) => (
          <ReviewCard
            key={review.id}
            review={review}
          />
        ))}
      </div>
    )}
</section>
      </div>
    </section>
  );
}

export default ProductDetails;