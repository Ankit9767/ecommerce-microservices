import React, {
  useEffect,
  useMemo,
  useState
} from "react";
import { useSearchParams } from "react-router-dom";

import ProductFilters from "../components/ProductFilters";
import ProductGrid from "../components/ProductGrid";
import { searchProducts } from "../services/searchService";

import "./styles/Products.css";

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
  const [totalElements, setTotalElements] =
    useState(0);

  const [isLoading, setIsLoading] =
    useState(true);

  const [error, setError] = useState("");

  const filters = useMemo(
    () => ({
      search: searchParams.get("q") || "",
      category:
        searchParams.get("category") || "",
      minPrice:
        searchParams.get("minPrice") || "",
      maxPrice:
        searchParams.get("maxPrice") || "",
      sortBy:
        searchParams.get("sort") || ""
    }),
    [searchParams]
  );

  useEffect(() => {
    let isMounted = true;

    async function loadProducts() {
      setIsLoading(true);
      setError("");

      try {
        const response = await searchProducts({
          q: filters.search || undefined,
          category:
            filters.category || undefined,
          minPrice:
            filters.minPrice || undefined,
          maxPrice:
            filters.maxPrice || undefined,
          active: true,
          page: 0,
          size: 20,
          sort:
            filters.sortBy || undefined
        });

        if (!isMounted) {
          return;
        }

        setProducts(
          (response.content || []).map(
            mapProductSearchResult
          )
        );

        setTotalElements(
          response.totalElements || 0
        );
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setError(
          requestError.message ||
            "Unable to load products."
        );
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadProducts();

    return () => {
      isMounted = false;
    };
  }, [filters]);

  const handleFilterChange = (
    name,
    value
  ) => {
    const nextParams = new URLSearchParams(
      searchParams
    );

    if (value) {
      const parameterMap = {
        search: "q",
        category: "category",
        minPrice: "minPrice",
        maxPrice: "maxPrice",
        sortBy: "sort"
      };

      nextParams.set(
        parameterMap[name],
        value
      );
    } else {
      const parameterMap = {
        search: "q",
        category: "category",
        minPrice: "minPrice",
        maxPrice: "maxPrice",
        sortBy: "sort"
      };

      nextParams.delete(
        parameterMap[name]
      );
    }

    setSearchParams(nextParams);
  };

  const clearFilters = () => {
    setSearchParams({});
  };

  return (
    <section className="page products-page">
      <div className="container">
        <h1 className="page-title">
          Products
        </h1>

        <ProductFilters
          values={filters}
          onChange={handleFilterChange}
          onClear={clearFilters}
        />

        {isLoading && (
          <p>Loading products...</p>
        )}

        {!isLoading && error && (
          <p
            className="products-error"
            role="alert"
          >
            {error}
          </p>
        )}

        {!isLoading && !error && (
          <>
            <p className="products-result-count">
              {totalElements} products found
            </p>

            <ProductGrid
              products={products}
            />
          </>
        )}
      </div>
    </section>
  );
}

export default Products;