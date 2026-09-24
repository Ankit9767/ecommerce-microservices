import React, { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

import ProductFilters from "../components/ProductFilters";
import ProductGrid from "../components/ProductGrid";

import { getCategories } from "../services/categoryService";
import { searchProducts } from "../services/searchService";

import "./styles/Products.css";

const DEFAULT_PAGE_SIZE = 12;
const SEARCH_DEBOUNCE_MS = 400;

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
  const [searchParams, setSearchParams] =
    useSearchParams();

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

  /*
   * Load categories.
   */
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

  /*
   * Debounced product search.
   */
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
    const nextParams = new URLSearchParams(
      searchParams
    );

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

    /*
     * Any filter or sort change starts
     * from the first page.
     */
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

  /*
   * ProductDocument.category is populated from
   * ProductCreatedEvent.category.
   *
   * ProductServiceImpl creates that event from
   * ProductResponse.category.
   *
   * Therefore the search filter uses the
   * category name.
   */
  const categoryOptions = categories.map(
    (item) => item.name
  );

  return (
    <section className="page products-page">
      <div className="container">
        <div className="products-header">
          <div className="products-header-content">
            <p className="products-eyebrow">
              EcommerceHub
            </p>

            <h1 className="page-title">
              Products
            </h1>

            <p className="products-description">
              Browse products available in
              EcommerceHub.
            </p>
          </div>

          {!isLoading && !error && (
            <p className="products-result-count">
              {totalElements}{" "}
              {totalElements === 1
                ? "product"
                : "products"}
            </p>
          )}
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

        {categoryError && (
          <p
            className="products-filter-error"
            role="alert"
          >
            {categoryError}
          </p>
        )}

        {isLoading && (
          <div className="products-status">
            <p>Loading products...</p>
          </div>
        )}

        {!isLoading && error && (
          <div
            className="products-status products-status-error"
            role="alert"
          >
            <p>{error}</p>
          </div>
        )}

        {!isLoading && !error && (
          <>
            <div className="products-results-header">
              <p className="products-count">
                {totalElements}{" "}
                {totalElements === 1
                  ? "product found"
                  : "products found"}
              </p>
            </div>

            <ProductGrid products={products} />

            {totalPages > 1 && (
              <nav
                className="products-pagination"
                aria-label="Product pagination"
              >
                <button
                  type="button"
                  className="button products-pagination-button"
                  onClick={() =>
                    handlePageChange(page - 1)
                  }
                  disabled={page === 0}
                >
                  Previous
                </button>

                <span className="products-pagination-status">
                  Page {page + 1} of {totalPages}
                </span>

                <button
                  type="button"
                  className="button products-pagination-button"
                  onClick={() =>
                    handlePageChange(page + 1)
                  }
                  disabled={
                    page >= totalPages - 1
                  }
                >
                  Next
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