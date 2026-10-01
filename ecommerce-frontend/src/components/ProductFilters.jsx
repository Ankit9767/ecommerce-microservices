
import React from "react";

import "./styles/ProductFilters.css";

function SearchIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="product-filter-icon"
    >
      <circle cx="11" cy="11" r="6.5" />
      <path d="m16 16 4.5 4.5" />
    </svg>
  );
}

function CategoryIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="product-filter-icon"
    >
      <rect x="4" y="4" width="6" height="6" rx="1" />
      <rect x="14" y="4" width="6" height="6" rx="1" />
      <rect x="4" y="14" width="6" height="6" rx="1" />
      <rect x="14" y="14" width="6" height="6" rx="1" />
    </svg>
  );
}

function PriceIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="product-filter-icon"
    >
      <path d="M12 3v18" />
      <path d="M16 7.5c-.8-1-2-1.5-4-1.5-2.2 0-4 1.1-4 2.8 0 4.2 8 2 8 6.2 0 1.8-1.8 3-4 3-2 0-3.3-.6-4-1.7" />
    </svg>
  );
}

function SortIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="product-filter-icon"
    >
      <path d="M8 5v14" />
      <path d="m5 8 3-3 3 3" />
      <path d="M16 19V5" />
      <path d="m13 16 3 3 3-3" />
    </svg>
  );
}

function ClearIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="product-filter-icon"
    >
      <path d="M6 6l12 12" />
      <path d="M18 6 6 18" />
    </svg>
  );
}

function ProductFilters({
  search = "",
  category = "",
  minPrice = "",
  maxPrice = "",
  sortBy = "",
  categories = [],
  categoriesLoading = false,
  onSearchChange,
  onCategoryChange,
  onMinPriceChange,
  onMaxPriceChange,
  onSortChange,
  onClear
}) {
  return (
    <div className="product-filters">
      <div className="product-filter-group product-search-group">
        <label
          className="product-filter-label"
          htmlFor="product-search"
        >
          <span>Search</span>
        </label>

        <div className="product-filter-control">
          <SearchIcon />

          <input
            id="product-search"
            className="product-filter-input"
            type="search"
            value={search}
            onChange={(event) =>
              onSearchChange(event.target.value)
            }
            placeholder="Search products..."
          />
        </div>
      </div>

      <div className="product-filter-group">
        <label
          className="product-filter-label"
          htmlFor="product-category"
        >
          <span>Category</span>
        </label>

        <div className="product-filter-control">
          <CategoryIcon />

          <select
            id="product-category"
            className="product-filter-input"
            value={category}
            onChange={(event) =>
              onCategoryChange(event.target.value)
            }
            disabled={categoriesLoading}
          >
            <option value="">
              {categoriesLoading
                ? "Loading categories..."
                : "All Categories"}
            </option>

            {categories.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="product-filter-group product-price-group">
        <span className="product-filter-label">
          <span>Price</span>
        </span>

        <div className="product-price-fields">
          <div className="product-filter-control">
            <span className="product-price-symbol">
              ₹
            </span>

            <input
              className="product-filter-input"
              type="number"
              min="0"
              step="0.01"
              value={minPrice}
              onChange={(event) =>
                onMinPriceChange(event.target.value)
              }
              placeholder="Min"
              aria-label="Minimum price"
            />
          </div>

          <span
            className="product-price-separator"
            aria-hidden="true"
          >
            –
          </span>

          <div className="product-filter-control">
            <span className="product-price-symbol">
              ₹
            </span>

            <input
              className="product-filter-input"
              type="number"
              min="0"
              step="0.01"
              value={maxPrice}
              onChange={(event) =>
                onMaxPriceChange(event.target.value)
              }
              placeholder="Max"
              aria-label="Maximum price"
            />
          </div>
        </div>
      </div>

      <div className="product-filter-group">
        <label
          className="product-filter-label"
          htmlFor="product-sort"
        >
          <span>Sort</span>
        </label>

        <div className="product-filter-control">
          <SortIcon />

          <select
            id="product-sort"
            className="product-filter-input"
            value={sortBy}
            onChange={(event) =>
              onSortChange(event.target.value)
            }
          >
            <option value="">
              Default
            </option>

            <option value="price,asc">
              Price: Low to High
            </option>

            <option value="price,desc">
              Price: High to Low
            </option>

            <option value="name,asc">
              Name: A-Z
            </option>

            <option value="name,desc">
              Name: Z-A
            </option>

            <option value="sku,asc">
              SKU: A-Z
            </option>

            <option value="category,asc">
              Category: A-Z
            </option>

            <option value="active,desc">
              Active First
            </option>
          </select>
        </div>
      </div>

      <button
        type="button"
        className="product-filter-clear"
        onClick={onClear}
      >
        <ClearIcon />
        <span>Clear Filters</span>
      </button>
    </div>
  );
}

export default ProductFilters;

