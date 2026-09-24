import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import ProductGrid from "../components/ProductGrid";

import { getCategory } from "../services/categoryService";
import { searchProducts } from "../services/searchService";

import "./styles/CategoryProducts.css";

const DEFAULT_PAGE_SIZE = 12;

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
          <div className="category-products-status">
            <p>Loading category...</p>
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
            <p>{error}</p>

            <Link
              className="button"
              to="/categories"
            >
              Back to Categories
            </Link>
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
          ← Back to Categories
        </Link>

        <header className="category-products-header">
          <p className="category-products-eyebrow">
            Category
          </p>

          <h1 className="page-title">
            {category.name}
          </h1>

          {category.description && (
            <p className="page-description">
              {category.description}
            </p>
          )}

          {!isLoading && !error && (
            <p className="category-products-count">
              {totalElements}{" "}
              {totalElements === 1
                ? "product"
                : "products"}
            </p>
          )}
        </header>

        {error && (
          <div
            className="category-products-status category-products-status-error"
            role="alert"
          >
            <p>{error}</p>
          </div>
        )}

        {isLoading && (
          <div className="category-products-status">
            <p>Loading products...</p>
          </div>
        )}

        {!isLoading && !error && (
          <>
            <ProductGrid products={products} />

            {totalPages > 1 && (
              <nav
                className="category-products-pagination"
                aria-label="Category product pagination"
              >
                <button
                  type="button"
                  className="button"
                  onClick={handlePreviousPage}
                  disabled={page === 0}
                >
                  Previous
                </button>

                <span className="category-products-pagination-status">
                  Page {page + 1} of {totalPages}
                </span>

                <button
                  type="button"
                  className="button"
                  onClick={handleNextPage}
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

export default CategoryProducts;