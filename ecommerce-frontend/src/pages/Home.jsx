import React, {
  useEffect,
  useState
} from "react";
import { Link } from "react-router-dom";

import ProductGrid from "../components/ProductGrid";

import { searchProducts } from "../services/searchService";
import { getCategories } from "../services/categoryService";
import CategoryCard from "../components/CategoryCard";

import "./styles/Home.css";

function Home() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] =
    useState([]);

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
        const response =
          await searchProducts({
            page: 0,
            size: 8
          });

        if (!isMounted) {
          return;
        }

        setProducts(
          response?.content || []
        );
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
        const response =
          await getCategories();

        if (!isMounted) {
          return;
        }

        const categoryList =
          Array.isArray(response)
            ? response
            : response?.content || [];

        setCategories(
          categoryList.slice(0, 4)
        );
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
      <section className="home-hero">
        <div className="container">
          <div className="home-hero-content">
            <p className="home-hero-eyebrow">
              Welcome to EcommerceHub
            </p>

            <h1 className="home-hero-title">
              Discover products you'll love.
            </h1>

            <p className="home-hero-description">
              Explore quality products across
              electronics, fashion, home, beauty,
              and more.
            </p>

            <div className="home-hero-actions">
              <Link
                className="button"
                to="/products"
              >
                Shop Products
              </Link>

              <Link
                className="home-secondary-button"
                to="/categories"
              >
                Browse Categories
              </Link>
            </div>
          </div>
        </div>
      </section>

      <section className="home-section">
        <div className="container">
          <div className="home-section-header">
            <div>
              <p className="home-section-eyebrow">
                Explore
              </p>

              <h2 className="home-section-title">
                Shop by Category
              </h2>
            </div>

            <Link
              className="home-section-link"
              to="/categories"
            >
              View all
            </Link>
          </div>

          {isCategoriesLoading && (
            <div
              className="home-state"
              aria-live="polite"
            >
              Loading categories...
            </div>
          )}

          {!isCategoriesLoading &&
            categoriesError && (
              <div className="home-state">
                <p className="home-error">
                  {categoriesError}
                </p>
              </div>
            )}

          {!isCategoriesLoading &&
            !categoriesError &&
            categories.length === 0 && (
              <div className="home-state">
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
                {categories.map((category) => (
                  <CategoryCard
                    key={category.id}
                    category={category}
                  />
                ))}
              </div>
            )}
        </div>
      </section>

      <section className="home-section home-products-section">
        <div className="container">
          <div className="home-section-header">
            <div>
              <p className="home-section-eyebrow">
                Discover
              </p>

              <h2 className="home-section-title">
                Products
              </h2>
            </div>

            <Link
              className="home-section-link"
              to="/products"
            >
              View all
            </Link>
          </div>

          {isProductsLoading && (
            <div
              className="home-state"
              aria-live="polite"
            >
              Loading products...
            </div>
          )}

          {!isProductsLoading &&
            productsError && (
              <div className="home-state">
                <p className="home-error">
                  {productsError}
                </p>

                <Link
                  className="button"
                  to="/products"
                >
                  Browse Products
                </Link>
              </div>
            )}

          {!isProductsLoading &&
            !productsError &&
            products.length === 0 && (
              <div className="home-state">
                <p>
                  No products are available
                  right now.
                </p>

                <Link
                  className="button"
                  to="/products"
                >
                  Browse Products
                </Link>
              </div>
            )}

          {!isProductsLoading &&
            !productsError &&
            products.length > 0 && (
              <ProductGrid
                products={products}
              />
            )}
        </div>
      </section>

      <section className="home-promotion">
        <div className="container">
          <div className="home-promotion-content">
            <div>
              <p className="home-section-eyebrow">
                EcommerceHub
              </p>

              <h2>
                Find something great for
                every part of your life.
              </h2>

              <p>
                Browse our growing collection
                and discover products for
                everyday shopping.
              </p>
            </div>

            <Link
              className="button"
              to="/products"
            >
              Start Shopping
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Home;