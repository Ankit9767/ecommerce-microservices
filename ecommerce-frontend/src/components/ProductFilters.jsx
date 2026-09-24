import React from "react";

import "./styles/ProductFilters.css";

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
          Search
        </label>

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

      <div className="product-filter-group">
        <label
          className="product-filter-label"
          htmlFor="product-category"
        >
          Category
        </label>

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

      <div className="product-filter-group">
        <span className="product-filter-label">
          Price
        </span>

        <div className="product-price-fields">
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

          <span className="product-price-separator">
            –
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

      <div className="product-filter-group">
        <label
          className="product-filter-label"
          htmlFor="product-sort"
        >
          Sort
        </label>

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

      <button
        type="button"
        className="product-filter-clear"
        onClick={onClear}
      >
        Clear Filters
      </button>
    </div>
  );
}

export default ProductFilters;