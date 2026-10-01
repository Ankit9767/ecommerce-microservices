
import React, { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

import ProductFilters from "../components/ProductFilters";
import ProductGrid from "../components/ProductGrid";

import { getCategories } from "../services/categoryService";
import { searchProducts } from "../services/searchService";

import "./styles/Products.css";

const DEFAULT_PAGE_SIZE = 12;
const SEARCH_DEBOUNCE_MS = 400;

function SearchIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="products-icon">
      <circle cx="11" cy="11" r="6.5" />
      <path d="m16 16 4.5 4.5" />
    </svg>
  );
}

function GridIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="products-icon">
      <rect x="4" y="4" width="6" height="6" rx="1" />
      <rect x="14" y="4" width="6" height="6" rx="1" />
      <rect x="4" y="14" width="6" height="6" rx="1" />
      <rect x="14" y="14" width="6" height="6" rx="1" />
    </svg>
  );
}

function PackageIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="products-icon">
      <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z" />
      <path d="m4 7.5 8 4.5 8-4.5" />
      <path d="M12 12v9" />
    </svg>
  );
}

function ArrowIcon({ direction = "right" }) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="products-icon"
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
    <svg viewBox="0 0 24 24" aria-hidden="true" className="products-icon">
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

function Products() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);

  const [isLoading, setIsLoading] = useState(true);
  const [isCategoriesLoading, setIsCategoriesLoading] =
    useState(true);

  const [error, setError] = useState("");
  const [categoryError, setCategoryError] = useState("");

  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const search = searchParams.get("search") || "";
  const category = searchParams.get("category") || "";
  const minPrice = searchParams.get("minPrice") || "";
  const maxPrice = searchParams.get("maxPrice") || "";
  const sortBy = searchParams.get("sort") || "";

  const pageValue = Number(searchParams.get("page"));

  const page =
    Number.isInteger(pageValue) && pageValue >= 0
      ? pageValue
      : 0;

  useEffect(() => {
    let isMounted = true;

    async function loadCategories() {
      setIsCategoriesLoading(true);
      setCategoryError("");

      try {
        const response = await getCategories();

        if (!isMounted) {
          return;
        }

        const categoryList = Array.isArray(response)
          ? response
          : response?.content || [];

        const activeCategories = categoryList.filter(
          (item) => item.active !== false
        );

        setCategories(activeCategories);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setCategories([]);

        setCategoryError(
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

  useEffect(() => {
    let isMounted = true;

    const timeoutId = setTimeout(() => {
      async function loadProducts() {
        setIsLoading(true);
        setError("");

        try {
          const response = await searchProducts({
            q: search || undefined,
            category: category || undefined,

            minPrice:
              minPrice !== ""
                ? minPrice
                : undefined,

            maxPrice:
              maxPrice !== ""
                ? maxPrice
                : undefined,

            active: true,

            page,
            size: DEFAULT_PAGE_SIZE,
            sort: sortBy || undefined
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
              "Unable to load products. Please try again."
          );
        } finally {
          if (isMounted) {
            setIsLoading(false);
          }
        }
      }

      loadProducts();
    }, SEARCH_DEBOUNCE_MS);

    return () => {
      isMounted = false;
      clearTimeout(timeoutId);
    };
  }, [
    search,
    category,
    minPrice,
    maxPrice,
    sortBy,
    page
  ]);

  const updateSearchParams = (updates) => {
    const nextParams = new URLSearchParams(searchParams);

    Object.entries(updates).forEach(
      ([key, value]) => {
        if (
          value === undefined ||
          value === null ||
          value === ""
        ) {
          nextParams.delete(key);
        } else {
          nextParams.set(key, String(value));
        }
      }
    );

    nextParams.set("page", "0");

    setSearchParams(nextParams);
  };

  const handleSearchChange = (value) => {
    updateSearchParams({
      search: value
    });
  };

  const handleCategoryChange = (value) => {
    updateSearchParams({
      category: value
    });
  };

  const handleMinPriceChange = (value) => {
    updateSearchParams({
      minPrice: value
    });
  };

  const handleMaxPriceChange = (value) => {
    updateSearchParams({
      maxPrice: value
    });
  };

  const handleSortChange = (value) => {
    updateSearchParams({
      sort: value
    });
  };

  const handleClear = () => {
    setSearchParams({
      page: "0"
    });
  };

  const handlePageChange = (nextPage) => {
    if (nextPage < 0) {
      return;
    }

    if (
      totalPages > 0 &&
      nextPage >= totalPages
    ) {
      return;
    }

    const nextParams = new URLSearchParams(
      searchParams
    );

    nextParams.set(
      "page",
      String(nextPage)
    );

    setSearchParams(nextParams);
  };

  const categoryOptions = categories.map(
    (item) => item.name
  );

  return (
    <section className="page products-page">
      <div className="container">
        <header className="products-header">
          <div className="products-header-content">
            <div className="products-title-row">
              <div className="products-title-icon">
                <PackageIcon />
              </div>

              <div>
                <p className="products-eyebrow">
                  EcommerceHub
                </p>

                <h1 className="page-title">
                  Products
                </h1>
              </div>
            </div>

            <p className="products-description">
              Discover products across our growing
              collection and find something that fits
              what you need.
            </p>

            <div className="products-header-meta">
              <span className="products-meta-item">
                <GridIcon />
                Browse the collection
              </span>

              <span className="products-meta-divider" />

              <span className="products-meta-item">
                <SearchIcon />
                Search and filter
              </span>
            </div>
          </div>

        </header>

        <div className="products-filter-section">
          <div className="products-filter-heading">
            <div className="products-filter-heading-icon">
              <SearchIcon />
            </div>

            <div>
              <h2>Find what you're looking for</h2>
              <p>
                Search, filter, and sort the available
                products.
              </p>
            </div>
          </div>

          <ProductFilters
            search={search}
            category={category}
            minPrice={minPrice}
            maxPrice={maxPrice}
            sortBy={sortBy}
            categories={categoryOptions}
            categoriesLoading={isCategoriesLoading}
            onSearchChange={handleSearchChange}
            onCategoryChange={handleCategoryChange}
            onMinPriceChange={handleMinPriceChange}
            onMaxPriceChange={handleMaxPriceChange}
            onSortChange={handleSortChange}
            onClear={handleClear}
          />
        </div>

        {categoryError && (
          <div
            className="products-filter-error"
            role="alert"
          >
            <AlertIcon />
            <p>{categoryError}</p>
          </div>
        )}

        {isLoading && (
          <div
            className="products-status products-loading-state"
            aria-live="polite"
          >
            <div className="products-loading-icon">
              <PackageIcon />
            </div>

            <div className="products-loading-content">
              <strong>Loading products</strong>
              <p>
                We're finding products that match
                your selection.
              </p>
            </div>

            <div
              className="products-loading-bar"
              aria-hidden="true"
            >
              <span />
            </div>
          </div>
        )}

        {!isLoading && error && (
          <div
            className="products-status products-status-error"
            role="alert"
          >
            <div className="products-state-icon">
              <AlertIcon />
            </div>

            <div>
              <h2>Unable to load products</h2>
              <p>{error}</p>
            </div>
          </div>
        )}

        {!isLoading && !error && (
          <>
            <div className="products-results-header">
              <div className="products-results-title">
                <span className="products-results-icon">
                  <GridIcon />
                </span>

                <div>
                  <p className="products-results-label">
                    Product collection
                  </p>

                  <p className="products-count">
                    {totalElements}{" "}
                    {totalElements === 1
                      ? "product found"
                      : "products found"}
                  </p>
                </div>
              </div>

              {sortBy && (
                <span className="products-active-sort">
                  Sorted results
                </span>
              )}
            </div>

            {products.length > 0 ? (
              <div className="products-grid-animation">
                <ProductGrid products={products} />
              </div>
            ) : (
              <div className="products-empty-state">
                <div className="products-empty-icon">
                  <SearchIcon />
                </div>

                <h2>No products found</h2>

                <p>
                  Try changing your search or filters
                  to find more products.
                </p>

                <button
                  type="button"
                  className="products-clear-button"
                  onClick={handleClear}
                >
                  Clear filters
                </button>
              </div>
            )}

            {totalPages > 1 && (
              <nav
                className="products-pagination"
                aria-label="Product pagination"
              >
                <button
                  type="button"
                  className="products-pagination-button"
                  onClick={() =>
                    handlePageChange(page - 1)
                  }
                  disabled={page === 0}
                >
                  <ArrowIcon direction="left" />
                  <span>Previous</span>
                </button>

                <div className="products-pagination-status">
                  <span>Page</span>
                  <strong>{page + 1}</strong>
                  <span>of</span>
                  <strong>{totalPages}</strong>
                </div>

                <button
                  type="button"
                  className="products-pagination-button"
                  onClick={() =>
                    handlePageChange(page + 1)
                  }
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

export default Products;

