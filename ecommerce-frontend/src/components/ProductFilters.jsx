import React from "react";

import "./styles/ProductFilters.css";

function ProductFilters({
  search,
  category,
  minPrice,
  maxPrice,
  minRating,
  sortBy,
  categories,
  onSearchChange,
  onCategoryChange,
  onMinPriceChange,
  onMaxPriceChange,
  onMinRatingChange,
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
        >
          <option value="">
            All Categories
          </option>

          {categories.map((item) => (
            <option
              key={item}
              value={item}
            >
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
          htmlFor="product-rating"
        >
          Rating
        </label>

        <select
          id="product-rating"
          className="product-filter-input"
          value={minRating}
          onChange={(event) =>
            onMinRatingChange(event.target.value)
          }
        >
          <option value="">
            Any Rating
          </option>

          <option value="4">
            4+ Stars
          </option>

          <option value="4.5">
            4.5+ Stars
          </option>
        </select>
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
          <option value="featured">
            Featured
          </option>

          <option value="price-low">
            Price: Low to High
          </option>

          <option value="price-high">
            Price: High to Low
          </option>

          <option value="rating">
            Rating
          </option>

          <option value="name">
            Name
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