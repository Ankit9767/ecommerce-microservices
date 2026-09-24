import React from "react";

import ProductCard from "./ProductCard";
import "./styles/ProductGrid.css";

function ProductGrid({ products }) {
  if (!products || products.length === 0) {
    return (
      <div className="product-grid-empty">
        <p>No products found.</p>
      </div>
    );
  }

  return (
    <div className="product-grid">
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
        />
      ))}
    </div>
  );
}

export default ProductGrid;