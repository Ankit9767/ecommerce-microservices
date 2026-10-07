import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import QuantitySelector from "../components/QuantitySelector";
import ReviewCard from "../components/ReviewCard";

import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

import { apiGetBlob } from "../services/api";

import { getProduct, getProductImages } from "../services/productService";
import { getProductReviews } from "../services/reviewService";

import "./styles/ProductDetails.css";

function ArrowIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M5 12h14" />
      <path d="m13 6 6 6-6 6" />
    </svg>
  );
}

function BackIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M19 12H5" />
      <path d="m11 18-6-6 6-6" />
    </svg>
  );
}

function TrashIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.9"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M3 6h18" />
      <path d="M8 6V4.5A1.5 1.5 0 0 1 9.5 3h5A1.5 1.5 0 0 1 16 4.5V6" />
      <path d="M19 6l-1 14H6L5 6" />
      <path d="M10 10v6" />
      <path d="M14 10v6" />
    </svg>
  );
}

function CartIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M4 5h2l1.6 9.1a2 2 0 0 0 2 1.7h6.8a2 2 0 0 0 1.9-1.4L20 8H7" />
      <circle cx="10" cy="19" r="1.2" />
      <circle cx="17" cy="19" r="1.2" />
    </svg>
  );
}

function PackageIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z" />
      <path d="m4 7.5 8 4.5 8-4.5" />
      <path d="M12 12v9" />
      <path d="m8 5.25 8 4.5" />
    </svg>
  );
}

function TagIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M20 13.5 13.5 20 4 10.5V4h6.5L20 13.5Z" />
      <circle cx="8" cy="8" r="1.2" />
    </svg>
  );
}

function SkuIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m12 3 7 4v10l-7 4-7-4V7l7-4Z" />
      <path d="m8.5 9 3.5-2 3.5 2-3.5 2-3.5-2Z" />
      <path d="M8.5 13 12 15l3.5-2" />
      <path d="M12 11v4" />
    </svg>
  );
}

function DescriptionIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M6 4h12a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z" />
      <path d="M8 9h8" />
      <path d="M8 13h8" />
      <path d="M8 17h5" />
    </svg>
  );
}

function StarIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="m12 3 2.78 5.63 6.22.9-4.5 4.39 1.06 6.2L12 17.2l-5.56 2.92 1.06-6.2L3 9.53l6.22-.9L12 3Z" />
    </svg>
  );
}

function ReviewIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M20 11.5a7.5 7.5 0 0 1-7.5 7.5H8l-4 2v-5.1A7.5 7.5 0 1 1 20 11.5Z" />
      <path d="M8 11h.01" />
      <path d="M12 11h.01" />
      <path d="M16 11h.01" />
    </svg>
  );
}

function ProductDetails() {
  const { id } = useParams();

  const { isAuthenticated } = useAuth();

  const { items, addToCart, updateQuantity, removeFromCart, isUpdating } =
    useCart();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [imageUrl, setImageUrl] = useState("");

  const [isLoading, setIsLoading] = useState(true);
  const [isAddingToCart, setIsAddingToCart] = useState(false);

  const [error, setError] = useState("");
  const [cartError, setCartError] = useState("");
  const [addedToCart, setAddedToCart] = useState(false);

  const [reviews, setReviews] = useState([]);
  const [isReviewsLoading, setIsReviewsLoading] = useState(false);
  const [reviewsError, setReviewsError] = useState("");
  const [reviewTotalElements, setReviewTotalElements] = useState(0);

  const cartItem = items.find((item) => String(item.productId) === String(id));

  const isInCart = Boolean(cartItem);
  const cartQuantity = Number(cartItem?.quantity || 0);

  useEffect(() => {
    let isMounted = true;
    let objectUrl = null;

    async function loadProductImage() {
      if (!product?.id) {
        setImageUrl("");
        return;
      }

      try {
        const images = await getProductImages(product.id);

        if (!isMounted) {
          return;
        }

        const primaryImage =
          images?.find((image) => image.primaryImage === true) || images?.[0];

        if (!primaryImage?.id) {
          setImageUrl("");
          return;
        }

        const imageBlob = await apiGetBlob(
          `/products/${product.id}/images/${primaryImage.id}/content`,
        );

        if (!isMounted) {
          return;
        }

        objectUrl = URL.createObjectURL(imageBlob);
        setImageUrl(objectUrl);
      } catch {
        if (isMounted) {
          setImageUrl("");
        }
      }
    }

    loadProductImage();

    return () => {
      isMounted = false;

      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [product?.id]);

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
            requestError.message || "Unable to load product. Please try again.",
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

  useEffect(() => {
    let isMounted = true;

    async function loadReviews() {
      if (!id) {
        return;
      }

      setIsReviewsLoading(true);
      setReviewsError("");

      try {
        const response = await getProductReviews(id, {
          page: 0,
          size: 20,
          sort: "createdAt,desc",
        });

        if (!isMounted) {
          return;
        }

        setReviews(response?.content || []);
        setReviewTotalElements(Number(response?.totalElements || 0));
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setReviewsError(
          requestError.message || "Unable to load product reviews.",
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

  useEffect(() => {
    if (isInCart) {
      setQuantity(cartQuantity);
      setAddedToCart(true);
    } else {
      setQuantity(1);
      setAddedToCart(false);
    }
  }, [isInCart, cartQuantity]);

  const handleQuantityChange = async (nextQuantity) => {
    setCartError("");

    if (!isInCart) {
      setQuantity(nextQuantity);
      setAddedToCart(false);
      return;
    }

    try {
      await updateQuantity(product.id, nextQuantity);
    } catch (requestError) {
      setCartError(
        requestError.message || "Unable to update the cart quantity.",
      );
    }
  };

  const handleAddToCart = async () => {
    if (
      !product ||
      quantity < 1 ||
      !isAuthenticated ||
      isAddingToCart ||
      isUpdating
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
        requestError.message || "Unable to add the product to your cart.",
      );
    } finally {
      setIsAddingToCart(false);
    }
  };

  const handleRemoveFromCart = async () => {
    if (!product || isUpdating) {
      return;
    }

    setCartError("");

    try {
      await removeFromCart(product.id);

      setQuantity(1);
      setAddedToCart(false);
    } catch (requestError) {
      setCartError(
        requestError.message || "Unable to remove the product from your cart.",
      );
    }
  };

  if (isLoading) {
    return (
      <section className="page product-details-page">
        <div className="container">
          <div className="product-details-status product-details-loading">
            <div className="product-details-loading-spinner" />
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
            <div className="product-details-status-icon">!</div>

            <p>{error}</p>

            <Link className="button product-details-back-button" to="/products">
              <BackIcon />
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

            <Link className="button product-details-back-button" to="/products">
              <BackIcon />
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
        <Link className="product-details-back-link" to="/products">
          <BackIcon />
          <span>Back to Products</span>
        </Link>

        <div className="product-details">
          <div className="product-details-image">
            {imageUrl ? (
              <img src={imageUrl} alt={product.name} />
            ) : (
              <div
                className="product-details-image-placeholder"
                aria-label={`${product.name} image placeholder`}
              >
                {product.name ? product.name.charAt(0).toUpperCase() : "P"}
              </div>
            )}

            <div className="product-details-image-shine" />
          </div>

          <div className="product-details-content">
            {product.category && (
              <p className="product-details-category">
                <TagIcon />
                {product.category}
              </p>
            )}

            <h1 className="product-details-title">{product.name}</h1>

            {product.sku && (
              <p className="product-details-sku">
                <span className="product-details-sku-icon">
                  <SkuIcon />
                </span>
                SKU: {product.sku}
              </p>
            )}

            <div className="product-details-price-row">
              <p className="product-details-price">
                {Number.isFinite(price)
                  ? `$${price.toFixed(2)}`
                  : "Price unavailable"}
              </p>

              {isInCart && (
                <span className="product-details-cart-status">
                  <CartIcon />
                  In your cart
                </span>
              )}
            </div>

            {product.rating !== null && product.rating !== undefined && (
              <div className="product-details-rating">
                <span className="product-details-rating-stars">
                  <StarIcon />
                </span>

                <strong>{Number(product.rating).toFixed(1)}</strong>

                {product.reviewCount !== undefined && (
                  <span>{Number(product.reviewCount)} reviews</span>
                )}
              </div>
            )}

            {product.description && (
              <div className="product-details-description">
                <div className="product-details-section-heading">
                  <span className="product-details-section-icon">
                    <DescriptionIcon />
                  </span>

                  <div>
                    <span className="product-details-section-eyebrow">
                      Product information
                    </span>

                    <h2>Description</h2>
                  </div>
                </div>

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
                <div className="product-details-purchase-heading">
                  <span>Quantity</span>

                  {isInCart && (
                    <span className="product-details-purchase-hint">
                      Update your cart quantity
                    </span>
                  )}
                </div>

                <div className="product-details-purchase-row">
                  <div className="product-details-purchase-controls">
                    <QuantitySelector
                      quantity={isInCart ? cartQuantity : quantity}
                      onQuantityChange={handleQuantityChange}
                      disabled={isAddingToCart || isUpdating}
                    />

                    {isInCart && (
                      <button
                        type="button"
                        className="product-details-remove-button"
                        onClick={handleRemoveFromCart}
                        disabled={isUpdating || isAddingToCart}
                        aria-label={`Remove ${product.name} from cart`}
                        title="Remove from cart"
                      >
                        <TrashIcon />
                      </button>
                    )}
                  </div>

                  {!isInCart &&
                    (isAuthenticated ? (
                      <button
                        type="button"
                        className="button product-details-cart-button"
                        onClick={handleAddToCart}
                        disabled={isAddingToCart || isUpdating}
                      >
                        <CartIcon />

                        <span>
                          {isAddingToCart ? "Adding..." : "Add to Cart"}
                        </span>

                        {!isAddingToCart && <ArrowIcon />}
                      </button>
                    ) : (
                      <Link
                        className="button product-details-cart-button"
                        to="/login"
                      >
                        <CartIcon />

                        <span>Sign In to Add to Cart</span>

                        <ArrowIcon />
                      </Link>
                    ))}

                  {isInCart && (
                    <Link
                      className="button product-details-view-cart-button"
                      to="/cart"
                    >
                      <CartIcon />

                      <span>View Cart</span>

                      <ArrowIcon />
                    </Link>
                  )}
                </div>

                {addedToCart && !cartError && (
                  <p className="product-details-cart-success" role="status">
                    <span>✓</span>
                    Product added to cart.
                  </p>
                )}

                {cartError && (
                  <p className="product-details-cart-error" role="alert">
                    <span>!</span>
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
                <ReviewIcon />
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
              className="product-reviews-state product-reviews-loading"
              aria-live="polite"
            >
              <span className="product-reviews-loading-dot" />
              Loading reviews...
            </div>
          )}

          {!isReviewsLoading && reviewsError && (
            <div className="product-reviews-state">
              <p className="product-reviews-error" role="alert">
                {reviewsError}
              </p>
            </div>
          )}

          {!isReviewsLoading && !reviewsError && reviews.length === 0 && (
            <div className="product-reviews-state">
              <ReviewIcon />

              <p>
                No reviews yet. Be the first customer to review this product
                after your purchase.
              </p>
            </div>
          )}

          {!isReviewsLoading && !reviewsError && reviews.length > 0 && (
            <div className="product-reviews-list">
              {reviews.map((review) => (
                <ReviewCard key={review.id} review={review} />
              ))}
            </div>
          )}
        </section>
      </div>
    </section>
  );
}

export default ProductDetails;
