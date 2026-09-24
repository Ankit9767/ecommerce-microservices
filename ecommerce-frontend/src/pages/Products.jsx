import React, { useMemo, useState } from "react";
import {
  useSearchParams
} from "react-router-dom";

import ProductFilters from "../components/ProductFilters";
import ProductGrid from "../components/ProductGrid";

import "./styles/Products.css";

const products = [
  {
    id: "product-1",
    name: "Wireless Headphones",
    price: 79.99,
    category: "Electronics",
    rating: 4.8
  },
  {
    id: "product-2",
    name: "Classic Sneakers",
    price: 64.99,
    category: "Fashion",
    rating: 4.6
  },
  {
    id: "product-3",
    name: "Smart Watch",
    price: 129.99,
    category: "Electronics",
    rating: 4.7
  },
  {
    id: "product-4",
    name: "Minimal Desk Lamp",
    price: 39.99,
    category: "Home & Living",
    rating: 4.5
  },
  {
    id: "product-5",
    name: "Cotton T-Shirt",
    price: 24.99,
    category: "Fashion",
    rating: 4.4
  },
  {
    id: "product-6",
    name: "Bluetooth Speaker",
    price: 59.99,
    category: "Electronics",
    rating: 4.7
  },
  {
    id: "product-7",
    name: "Leather Wallet",
    price: 34.99,
    category: "Fashion",
    rating: 4.5
  },
  {
    id: "product-8",
    name: "Ceramic Coffee Mug",
    price: 14.99,
    category: "Home & Living",
    rating: 4.3
  },
  {
    id: "product-9",
    name: "Face Care Set",
    price: 29.99,
    category: "Beauty",
    rating: 4.6
  },
  {
    id: "product-10",
    name: "Moisturizing Cream",
    price: 19.99,
    category: "Beauty",
    rating: 4.5
  }
];

const categories = [
  ...new Set(
    products.map((product) => product.category)
  )
];

function Products() {
  const [searchParams, setSearchParams] =
    useSearchParams();

  const [search, setSearch] = useState(
    searchParams.get("search") || ""
  );

  const [category, setCategory] = useState(
    searchParams.get("category") || ""
  );

  const [minPrice, setMinPrice] = useState(
    searchParams.get("minPrice") || ""
  );

  const [maxPrice, setMaxPrice] = useState(
    searchParams.get("maxPrice") || ""
  );

  const [minRating, setMinRating] = useState(
    searchParams.get("rating") || ""
  );

  const [sortBy, setSortBy] = useState(
    searchParams.get("sort") || "featured"
  );

  const updateUrl = (filters) => {
    const params = new URLSearchParams();

    Object.entries(filters).forEach(
      ([key, value]) => {
        if (value) {
          params.set(key, value);
        }
      }
    );

    setSearchParams(params);
  };

  const handleSearchChange = (value) => {
    setSearch(value);

    updateUrl({
      search: value,
      category,
      minPrice,
      maxPrice,
      rating: minRating,
      sort: sortBy
    });
  };

  const handleCategoryChange = (value) => {
    setCategory(value);

    updateUrl({
      search,
      category: value,
      minPrice,
      maxPrice,
      rating: minRating,
      sort: sortBy
    });
  };

  const handleMinPriceChange = (value) => {
    setMinPrice(value);

    updateUrl({
      search,
      category,
      minPrice: value,
      maxPrice,
      rating: minRating,
      sort: sortBy
    });
  };

  const handleMaxPriceChange = (value) => {
    setMaxPrice(value);

    updateUrl({
      search,
      category,
      minPrice,
      maxPrice: value,
      rating: minRating,
      sort: sortBy
    });
  };

  const handleMinRatingChange = (value) => {
    setMinRating(value);

    updateUrl({
      search,
      category,
      minPrice,
      maxPrice,
      rating: value,
      sort: sortBy
    });
  };

  const handleSortChange = (value) => {
    setSortBy(value);

    updateUrl({
      search,
      category,
      minPrice,
      maxPrice,
      rating: minRating,
      sort: value
    });
  };

  const handleClear = () => {
    setSearch("");
    setCategory("");
    setMinPrice("");
    setMaxPrice("");
    setMinRating("");
    setSortBy("featured");

    setSearchParams({});
  };

  const filteredProducts = useMemo(() => {
    const normalizedSearch =
      search.trim().toLowerCase();

    const minimumPrice = minPrice
      ? Number(minPrice)
      : null;

    const maximumPrice = maxPrice
      ? Number(maxPrice)
      : null;

    const minimumRating = minRating
      ? Number(minRating)
      : null;

    const result = products.filter((product) => {
      const matchesSearch =
        !normalizedSearch ||
        product.name
          .toLowerCase()
          .includes(normalizedSearch);

      const matchesCategory =
        !category ||
        product.category === category;

      const matchesMinPrice =
        minimumPrice === null ||
        product.price >= minimumPrice;

      const matchesMaxPrice =
        maximumPrice === null ||
        product.price <= maximumPrice;

      const matchesRating =
        minimumRating === null ||
        product.rating >= minimumRating;

      return (
        matchesSearch &&
        matchesCategory &&
        matchesMinPrice &&
        matchesMaxPrice &&
        matchesRating
      );
    });

    return [...result].sort(
      (firstProduct, secondProduct) => {
        if (sortBy === "price-low") {
          return (
            firstProduct.price -
            secondProduct.price
          );
        }

        if (sortBy === "price-high") {
          return (
            secondProduct.price -
            firstProduct.price
          );
        }

        if (sortBy === "rating") {
          return (
            secondProduct.rating -
            firstProduct.rating
          );
        }

        if (sortBy === "name") {
          return firstProduct.name.localeCompare(
            secondProduct.name
          );
        }

        return 0;
      }
    );
  }, [
    search,
    category,
    minPrice,
    maxPrice,
    minRating,
    sortBy
  ]);

  return (
    <section className="products-page">
      <div className="container">
        <div className="products-header">
          <div className="products-header-content">
            <p className="products-eyebrow">
              Explore our collection
            </p>

            <h1 className="page-title">
              Products
            </h1>

            <p className="products-description">
              Search, filter, and discover products
              from our collection.
            </p>
          </div>
        </div>

        <ProductFilters
          search={search}
          category={category}
          minPrice={minPrice}
          maxPrice={maxPrice}
          minRating={minRating}
          sortBy={sortBy}
          categories={categories}
          onSearchChange={handleSearchChange}
          onCategoryChange={handleCategoryChange}
          onMinPriceChange={handleMinPriceChange}
          onMaxPriceChange={handleMaxPriceChange}
          onMinRatingChange={handleMinRatingChange}
          onSortChange={handleSortChange}
          onClear={handleClear}
        />

        <div className="products-results-header">
          <p className="products-count">
            {filteredProducts.length}{" "}
            {filteredProducts.length === 1
              ? "product"
              : "products"}{" "}
            found
          </p>
        </div>

        <ProductGrid
          products={filteredProducts}
        />
      </div>
    </section>
  );
}

export default Products;