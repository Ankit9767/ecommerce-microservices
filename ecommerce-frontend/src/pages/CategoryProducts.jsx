
import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import ProductGrid from "../components/ProductGrid";

import { getCategory } from "../services/categoryService";
import { searchProducts } from "../services/searchService";

import "./styles/CategoryProducts.css";

const DEFAULT_PAGE_SIZE = 12;

function CategoryIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="category-products-icon"
    >
      <rect x="4" y="4" width="6" height="6" rx="1" />
      <rect x="14" y="4" width="6" height="6" rx="1" />
      <rect x="4" y="14" width="6" height="6" rx="1" />
      <rect x="14" y="14" width="6" height="6" rx="1" />
    </svg>
  );
}

function GridIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="category-products-icon"
    >
      <rect x="3.5" y="3.5" width="7" height="7" rx="1.5" />
      <rect x="13.5" y="3.5" width="7" height="7" rx="1.5" />
      <rect x="3.5" y="13.5" width="7" height="7" rx="1.5" />
      <rect x="13.5" y="13.5" width="7" height="7" rx="1.5" />
    </svg>
  );
}

function ArrowIcon({ direction = "right" }) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="category-products-icon"
    >
      {direction === "left" ? (
        <>
          <path d="M19 12H5" />
          <path d="m11 18-6-6 6-6" />
        </>
      ) : (
        <>
          <path d="M5 12h14" />
          <path d="m13 6 6 6-6 6" />
        </>
      )}
    </svg>
  );
}

function AlertIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="category-products-icon"
    >
      <path d="M12 4 21 20H3L12 4Z" />
      <path d="M12 9v5" />
      <path d="M12 17h.01" />
    </svg>
  );
}

function mapProductSearchResult(product) {
  return {
    id: product.productId,
    name: product.name,
    sku: product.sku,
    category: product.category,
    price: Number(product.price),
    image: product.primaryImageUrl,
    rating: product.averageRating,
    reviewCount: product.reviewCount,
    active: product.active
  };
}

function CategoryProducts() {
  const { id } = useParams();

  const [category, setCategory] = useState(null);
  const [products, setProducts] = useState([]);

  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadCategoryProducts() {
      setIsLoading(true);
      setError("");
      setCategory(null);
      setProducts([]);
      setPage(0);
      setTotalElements(0);
      setTotalPages(0);

      try {
        const categoryResponse = await getCategory(id);

        if (!isMounted) {
          return;
        }

        if (!categoryResponse) {
          setError("Category not found.");
          return;
        }

        if (categoryResponse.active === false) {
          setError("This category is currently unavailable.");
          return;
        }

        setCategory(categoryResponse);

        const productResponse = await searchProducts({
          category: categoryResponse.name,
          active: true,
          page: 0,
          size: DEFAULT_PAGE_SIZE
        });

        if (!isMounted) {
          return;
        }

        const mappedProducts = (
          productResponse?.content || []
        ).map(mapProductSearchResult);

        setProducts(mappedProducts);

        setTotalElements(
          productResponse?.totalElements || 0
        );

        setTotalPages(
          productResponse?.totalPages || 0
        );
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        if (requestError.status === 404) {
          setError("Category not found.");
        } else {
          setError(
            requestError.message ||
              "Unable to load category products. Please try again."
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
      setError("Category ID is missing.");

      return () => {
        isMounted = false;
      };
    }

    loadCategoryProducts();

    return () => {
      isMounted = false;
    };
  }, [id]);

  useEffect(() => {
    if (!category || page === 0) {
      return;
    }

    let isMounted = true;

    async function loadProductsPage() {
      setIsLoading(true);
      setError("");

      try {
        const response = await searchProducts({
          category: category.name,
          active: true,
          page,
          size: DEFAULT_PAGE_SIZE
        });

        if (!isMounted) {
          return;
        }

        const mappedProducts = (
          response?.content || []
        ).map(mapProductSearchResult);

        setProducts(mappedProducts);

        setTotalElements(
          response?.totalElements || 0
        );

        setTotalPages(
          response?.totalPages || 0
        );
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setProducts([]);
        setTotalElements(0);
        setTotalPages(0);

        setError(
          requestError.message ||
            "Unable to load category products. Please try again."
        );
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadProductsPage();

    return () => {
      isMounted = false;
    };
  }, [category, page]);

  const handlePreviousPage = () => {
    if (page <= 0) {
      return;
    }

    setPage((currentPage) => currentPage - 1);
  };

  const handleNextPage = () => {
    if (
      totalPages === 0 ||
      page >= totalPages - 1
    ) {
      return;
    }

    setPage((currentPage) => currentPage + 1);
  };

  if (isLoading && !category) {
    return (
      <section className="page category-products-page">
        <div className="container">
          <div className="category-products-loading">
            <div className="category-products-loading-icon">
              <CategoryIcon />
            </div>

            <strong>Loading category</strong>

            <p>
              We're finding products in this category.
            </p>

            <div
              className="category-products-loading-bar"
              aria-hidden="true"
            >
              <span />
            </div>
          </div>
        </div>
      </section>
    );
  }

  if (error && !category) {
    return (
      <section className="page category-products-page">
        <div className="container">
          <div
            className="category-products-status category-products-status-error"
            role="alert"
          >
            <div className="category-products-state-icon">
              <AlertIcon />
            </div>

            <div className="category-products-status-content">
              <h2>Unable to load category</h2>

              <p>{error}</p>

              <Link
                className="category-products-back-button"
                to="/categories"
              >
                <ArrowIcon direction="left" />
                <span>Back to Categories</span>
              </Link>
            </div>
          </div>
        </div>
      </section>
    );
  }

  if (!category) {
    return null;
  }

  return (
    <section className="page category-products-page">
      <div className="container">
        <Link
          className="category-products-back-link"
          to="/categories"
        >
          <ArrowIcon direction="left" />
          <span>Back to Categories</span>
        </Link>

        <header className="category-products-header">
          <div className="category-products-header-content">
            <div className="category-products-title-row">
              <div className="category-products-title-icon">
                <CategoryIcon />
              </div>

              <div>
                <p className="category-products-eyebrow">
                  Category
                </p>

                <h1 className="page-title">
                  {category.name}
                </h1>
              </div>
            </div>

            {category.description && (
              <p className="category-products-description">
                {category.description}
              </p>
            )}

            <div className="category-products-header-meta">
              <span className="category-products-meta-item">
                <GridIcon />
                Browse products
              </span>
            </div>
          </div>
        </header>

        {error && (
          <div
            className="category-products-filter-error"
            role="alert"
          >
            <AlertIcon />
            <p>{error}</p>
          </div>
        )}

        {isLoading && (
          <div
            className="category-products-loading-state"
            aria-live="polite"
          >
            <div className="category-products-loading-icon-small">
              <CategoryIcon />
            </div>

            <div className="category-products-loading-content">
              <strong>Loading products</strong>

              <p>
                We're finding products in this category.
              </p>
            </div>

            <div
              className="category-products-loading-bar"
              aria-hidden="true"
            >
              <span />
            </div>
          </div>
        )}

        {!isLoading && !error && (
          <>
            {products.length > 0 ? (
              <div className="category-products-grid-animation">
                <ProductGrid products={products} />
              </div>
            ) : (
              <div className="category-products-empty">
                <div className="category-products-empty-icon">
                  <CategoryIcon />
                </div>

                <h2>No products found</h2>

                <p>
                  There are currently no products available
                  in this category.
                </p>

                <Link
                  className="category-products-empty-button"
                  to="/categories"
                >
                  <ArrowIcon direction="left" />
                  <span>Browse Categories</span>
                </Link>
              </div>
            )}

            {totalPages > 1 && (
              <nav
                className="category-products-pagination"
                aria-label="Category product pagination"
              >
                <button
                  type="button"
                  className="category-products-pagination-button"
                  onClick={handlePreviousPage}
                  disabled={page === 0}
                >
                  <ArrowIcon direction="left" />
                  <span>Previous</span>
                </button>

                <div className="category-products-pagination-status">
                  <span>Page</span>
                  <strong>{page + 1}</strong>
                  <span>of</span>
                  <strong>{totalPages}</strong>
                </div>

                <button
                  type="button"
                  className="category-products-pagination-button"
                  onClick={handleNextPage}
                  disabled={
                    page >= totalPages - 1
                  }
                >
                  <span>Next</span>
                  <ArrowIcon />
                </button>
              </nav>
            )}
          </>
        )}
      </div>
    </section>
  );
}

export default CategoryProducts;
