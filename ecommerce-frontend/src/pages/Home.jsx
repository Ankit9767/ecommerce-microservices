
import React, {
  useEffect,
  useState
} from "react";
import { Link } from "react-router-dom";

import ProductGrid from "../components/ProductGrid";
import CategoryCard from "../components/CategoryCard";

import { searchProducts } from "../services/searchService";
import { getCategories } from "../services/categoryService";

import "./styles/Home.css";

function ArrowIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="home-icon"
    >
      <path d="M5 12h14" />
      <path d="m13 6 6 6-6 6" />
    </svg>
  );
}

function ShoppingBagIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="home-icon"
    >
      <path d="M6 8h12l1 12H5L6 8Z" />
      <path d="M9 8V6a3 3 0 0 1 6 0v2" />
    </svg>
  );
}

function SparkleIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="home-icon"
    >
      <path d="m12 3 1.4 5.6L19 10l-5.6 1.4L12 17l-1.4-5.6L5 10l5.6-1.4L12 3Z" />
      <path d="m19 16 .6 2.4L22 19l-2.4.6L19 22l-.6-2.4L16 19l2.4-.6L19 16Z" />
    </svg>
  );
}

function GridIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="home-icon"
    >
      <rect x="4" y="4" width="6" height="6" rx="1" />
      <rect x="14" y="4" width="6" height="6" rx="1" />
      <rect x="4" y="14" width="6" height="6" rx="1" />
      <rect x="14" y="14" width="6" height="6" rx="1" />
    </svg>
  );
}

function PackageIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="home-icon"
    >
      <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z" />
      <path d="m4 7.5 8 4.5 8-4.5" />
      <path d="M12 12v9" />
    </svg>
  );
}

function Home() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);

  const [isProductsLoading, setIsProductsLoading] =
    useState(true);

  const [isCategoriesLoading, setIsCategoriesLoading] =
    useState(true);

  const [productsError, setProductsError] =
    useState("");

  const [categoriesError, setCategoriesError] =
    useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadProducts() {
      setIsProductsLoading(true);
      setProductsError("");

      try {
        const response = await searchProducts({
          page: 0,
          size: 8
        });

        if (!isMounted) {
          return;
        }

        setProducts(response?.content || []);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setProductsError(
          requestError.message ||
            "Unable to load products."
        );
      } finally {
        if (isMounted) {
          setIsProductsLoading(false);
        }
      }
    }

    loadProducts();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;

    async function loadCategories() {
      setIsCategoriesLoading(true);
      setCategoriesError("");

      try {
        const response = await getCategories();

        if (!isMounted) {
          return;
        }

        const categoryList = Array.isArray(response)
          ? response
          : response?.content || [];

        setCategories(categoryList.slice(0, 4));
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setCategoriesError(
          requestError.message ||
            "Unable to load categories."
        );
      } finally {
        if (isMounted) {
          setIsCategoriesLoading(false);
        }
      }
    }

    loadCategories();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <div className="home-page">

      {/* HERO */}
      <section className="home-hero">
        <div className="container">
          <div className="home-hero-layout">

            <div className="home-hero-content">
              <div className="home-hero-eyebrow">
                <span className="home-eyebrow-icon">
                  <SparkleIcon />
                </span>

                <span>
                  Welcome to EcommerceHub
                </span>
              </div>

              <h1 className="home-hero-title">
                Discover products
                <span className="home-title-accent">
                  {" "}you'll love.
                </span>
              </h1>

              <p className="home-hero-description">
                Explore quality products across
                electronics, fashion, home, beauty,
                and more — all in one place.
              </p>

              <div className="home-hero-actions">
                <Link
                  className="home-primary-button"
                  to="/products"
                >
                  <ShoppingBagIcon />

                  <span>
                    Shop Products
                  </span>

                  <ArrowIcon />
                </Link>

                <Link
                  className="home-secondary-button"
                  to="/categories"
                >
                  <GridIcon />

                  <span>
                    Browse Categories
                  </span>
                </Link>
              </div>

              <div className="home-hero-note">
                <span className="home-note-dot" />
                Explore our growing collection
              </div>
            </div>

            <div
              className="home-hero-visual"
              aria-hidden="true"
            >
              <div className="hero-orbit hero-orbit-one" />
              <div className="hero-orbit hero-orbit-two" />

              <div className="hero-floating-card hero-card-top">
                <PackageIcon />
                <span>Products</span>
              </div>

              <div className="hero-main-icon">
                <ShoppingBagIcon />
              </div>

              <div className="hero-floating-card hero-card-bottom">
                <SparkleIcon />
                <span>Discover</span>
              </div>

              <div className="hero-dot hero-dot-one" />
              <div className="hero-dot hero-dot-two" />
              <div className="hero-dot hero-dot-three" />
            </div>

          </div>
        </div>
      </section>

      {/* CATEGORIES */}
      <section className="home-section home-category-section">
        <div className="container">

          <div className="home-section-header">
            <div className="home-section-heading">
              <div className="home-section-icon">
                <GridIcon />
              </div>

              <div>
                <p className="home-section-eyebrow">
                  Explore
                </p>

                <h2 className="home-section-title">
                  Shop by Category
                </h2>
              </div>
            </div>

            <Link
              className="home-section-link"
              to="/categories"
            >
              <span>View all</span>
              <ArrowIcon />
            </Link>
          </div>

          {isCategoriesLoading && (
            <div
              className="home-category-loading"
              aria-live="polite"
            >
              {[1, 2, 3, 4].map((item) => (
                <div
                  className="home-category-skeleton"
                  key={item}
                >
                  <div className="skeleton-icon" />
                  <div className="skeleton-line" />
                  <div className="skeleton-line short" />
                </div>
              ))}
            </div>
          )}

          {!isCategoriesLoading &&
            categoriesError && (
              <div className="home-state home-error-state">
                <div className="home-state-icon">
                  !
                </div>

                <p className="home-error">
                  {categoriesError}
                </p>
              </div>
            )}

          {!isCategoriesLoading &&
            !categoriesError &&
            categories.length === 0 && (
              <div className="home-state">
                <div className="home-state-icon">
                  <GridIcon />
                </div>

                <p>
                  No categories are available
                  right now.
                </p>
              </div>
            )}

          {!isCategoriesLoading &&
            !categoriesError &&
            categories.length > 0 && (
              <div className="home-category-grid">
                {categories.map((category, index) => (
                  <div
                    className="home-category-item"
                    key={category.id}
                    style={{
                      "--animation-delay":
                        `${index * 80}ms`
                    }}
                  >
                    <CategoryCard
                      category={category}
                    />
                  </div>
                ))}
              </div>
            )}

        </div>
      </section>

      {/* PRODUCTS */}
      <section className="home-section home-products-section">
        <div className="container">

          <div className="home-section-header">
            <div className="home-section-heading">
              <div className="home-section-icon">
                <PackageIcon />
              </div>

              <div>
                <p className="home-section-eyebrow">
                  Discover
                </p>

                <h2 className="home-section-title">
                  Products
                </h2>
              </div>
            </div>

            <Link
              className="home-section-link"
              to="/products"
            >
              <span>View all</span>
              <ArrowIcon />
            </Link>
          </div>

          {isProductsLoading && (
            <div
              className="home-state home-loading-state"
              aria-live="polite"
            >
              <div className="home-spinner" />

              <p>
                Loading products...
              </p>
            </div>
          )}

          {!isProductsLoading &&
            productsError && (
              <div className="home-state">
                <div className="home-state-icon">
                  !
                </div>

                <p className="home-error">
                  {productsError}
                </p>

                <Link
                  className="home-primary-button"
                  to="/products"
                >
                  <span>
                    Browse Products
                  </span>
                  <ArrowIcon />
                </Link>
              </div>
            )}

          {!isProductsLoading &&
            !productsError &&
            products.length === 0 && (
              <div className="home-state">
                <div className="home-state-icon">
                  <PackageIcon />
                </div>

                <p>
                  No products are available
                  right now.
                </p>

                <Link
                  className="home-primary-button"
                  to="/products"
                >
                  <span>
                    Browse Products
                  </span>
                  <ArrowIcon />
                </Link>
              </div>
            )}

          {!isProductsLoading &&
            !productsError &&
            products.length > 0 && (
              <div className="home-products-wrapper">
                <ProductGrid
                  products={products}
                />
              </div>
            )}

        </div>
      </section>

      {/* PROMOTION / CTA */}
      <section className="home-promotion">
        <div className="container">
          <div className="home-discovery-banner">
            <div className="home-discovery-decoration">
              <div className="discovery-circle discovery-circle-one" />

              <div className="discovery-circle discovery-circle-two" />

              <div className="discovery-mini-icon">
                <SparkleIcon />
              </div>
            </div>

            <div className="home-discovery-content">
              <div className="home-discovery-label">
                <span className="home-discovery-label-icon">
                  <SparkleIcon />
                </span>

                <span>
                  Keep exploring
                </span>
              </div>

              <h2>
                Your next favorite
                <span> find is waiting.</span>
              </h2>

              <p>
                Discover more products and explore
                everything EcommerceHub has to offer.
              </p>
            </div>

            <Link
              className="home-discovery-button"
              to="/products"
            >
              <span>
                Explore Products
              </span>

              <span className="home-discovery-arrow">
                <ArrowIcon />
              </span>
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Home;

