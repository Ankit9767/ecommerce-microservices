import React, { useState } from "react";

import ProductGrid from "../components/ProductGrid";

import "./styles/Products.css";

const products = [
  {
    id: "product-1",
    name: "Wireless Headphones",
    price: "$79.99",
    category: "Electronics",
    rating: "4.8"
  },
  {
    id: "product-2",
    name: "Classic Sneakers",
    price: "$64.99",
    category: "Fashion",
    rating: "4.6"
  },
  {
    id: "product-3",
    name: "Smart Watch",
    price: "$129.99",
    category: "Electronics",
    rating: "4.7"
  },
  {
    id: "product-4",
    name: "Minimal Desk Lamp",
    price: "$39.99",
    category: "Home & Living",
    rating: "4.5"
  },
  {
    id: "product-5",
    name: "Cotton T-Shirt",
    price: "$24.99",
    category: "Fashion",
    rating: "4.4"
  },
  {
    id: "product-6",
    name: "Bluetooth Speaker",
    price: "$59.99",
    category: "Electronics",
    rating: "4.7"
  },
  {
    id: "product-7",
    name: "Leather Wallet",
    price: "$34.99",
    category: "Fashion",
    rating: "4.5"
  },
  {
    id: "product-8",
    name: "Ceramic Coffee Mug",
    price: "$14.99",
    category: "Home & Living",
    rating: "4.3"
  }
];

function Products() {
  const [sortBy, setSortBy] = useState("featured");

  const sortedProducts = [...products].sort((firstProduct, secondProduct) => {
    if (sortBy === "price-low") {
      return (
        parseFloat(firstProduct.price.replace("$", "")) -
        parseFloat(secondProduct.price.replace("$", ""))
      );
    }

    if (sortBy === "price-high") {
      return (
        parseFloat(secondProduct.price.replace("$", "")) -
        parseFloat(firstProduct.price.replace("$", ""))
      );
    }

    if (sortBy === "rating") {
      return (
        parseFloat(secondProduct.rating) -
        parseFloat(firstProduct.rating)
      );
    }

    return 0;
  });

  return (
    <section className="products-page">
      <div className="container">
        <div className="products-header">
          <div className="products-header-content">
            <h1 className="page-title">
              Products
            </h1>

            <p className="products-count">
              {products.length} products available
            </p>
          </div>

          <div className="products-controls">
            <label htmlFor="product-sort">
              <span className="sr-only">
                Sort products
              </span>

              <select
                id="product-sort"
                className="products-control"
                value={sortBy}
                onChange={(event) => setSortBy(event.target.value)}
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
              </select>
            </label>
          </div>
        </div>

        <ProductGrid products={sortedProducts} />
      </div>
    </section>
  );
}

export default Products;