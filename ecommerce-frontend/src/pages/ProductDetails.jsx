import React, {
  useEffect,
  useState
} from "react";
import {
  Link,
  useParams
} from "react-router-dom";

import QuantitySelector from "../components/QuantitySelector";
import { useCart } from "../context/CartContext";
import { getProduct } from "../services/productService";

import "./styles/ProductDetails.css";

function ProductDetails() {
  const { id } = useParams();
  const { addToCart } = useCart();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);

  const [isLoading, setIsLoading] =
    useState(true);

  const [error, setError] = useState("");

  const [addedToCart, setAddedToCart] =
    useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadProduct() {
      setIsLoading(true);
      setError("");
      setProduct(null);

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

        setError(
          requestError.message ||
            "Unable to load product."
        );
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

  const handleAddToCart = () => {
    addToCart(product, quantity);
    setAddedToCart(true);
  };

  if (isLoading) {
    return (
      <section className="page product-details-page">
        <div className="container">
          <p>Loading product...</p>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="page product-details-page">
        <div className="container">
          <div className="product-details-error">
            <h1 className="page-title">
              Unable to load product
            </h1>

            <p>{error}</p>

            <Link
              className="button"
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
          <div className="product-details-not-found">
            <h1 className="page-title">
              Product Not Found
            </h1>

            <p>
              The product you're looking for
              could not be found.
            </p>

            <Link
              className="button"
              to="/products"
            >
              Back to Products
            </Link>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="page product-details-page">
      <div className="container">
        <Link
          className="product-details-back"
          to="/products"
        >
          ← Back to Products
        </Link>

        <div className="product-details">
          <div className="product-details-image">
            <div className="product-details-image-placeholder">
              {product.name.charAt(0)}
            </div>
          </div>

          <div className="product-details-content">
            <p className="product-details-category">
              {product.category}
            </p>

            <h1 className="page-title">
              {product.name}
            </h1>

            {product.sku && (
              <p className="product-details-sku">
                SKU: {product.sku}
              </p>
            )}

            <p className="product-details-price">
              ${Number(product.price).toFixed(2)}
            </p>

            {product.description && (
              <p className="product-details-description">
                {product.description}
              </p>
            )}

            <div className="product-details-actions">
              <QuantitySelector
                value={quantity}
                onChange={setQuantity}
                min={1}
              />

              <button
                type="button"
                className="button"
                onClick={handleAddToCart}
              >
                Add to Cart
              </button>
            </div>

            {addedToCart && (
              <p
                className="product-details-success"
                role="status"
              >
                Product added to cart.
              </p>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

export default ProductDetails;