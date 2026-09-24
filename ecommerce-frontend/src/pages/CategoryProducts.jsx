import React from "react";
import { Link, useParams } from "react-router-dom";

import ProductGrid from "../components/ProductGrid";

import "./styles/CategoryProducts.css";

const categories = [
  {
    id: "electronics",
    name: "Electronics",
    description:
      "Latest devices, gadgets, accessories, and everyday technology."
  },
  {
    id: "fashion",
    name: "Fashion",
    description:
      "Clothing, footwear, accessories, and styles for every occasion."
  },
  {
    id: "home",
    name: "Home & Living",
    description:
      "Furniture, lighting, kitchen essentials, and products for your home."
  },
  {
    id: "beauty",
    name: "Beauty",
    description:
      "Personal care, skincare, beauty essentials, and everyday wellness products."
  }
];

const products = [
  {
    id: "product-1",
    name: "Wireless Headphones",
    price: "$79.99",
    category: "Electronics",
    categoryId: "electronics",
    rating: "4.8"
  },
  {
    id: "product-2",
    name: "Classic Sneakers",
    price: "$64.99",
    category: "Fashion",
    categoryId: "fashion",
    rating: "4.6"
  },
  {
    id: "product-3",
    name: "Smart Watch",
    price: "$129.99",
    category: "Electronics",
    categoryId: "electronics",
    rating: "4.7"
  },
  {
    id: "product-4",
    name: "Minimal Desk Lamp",
    price: "$39.99",
    category: "Home & Living",
    categoryId: "home",
    rating: "4.5"
  },
  {
    id: "product-5",
    name: "Cotton T-Shirt",
    price: "$24.99",
    category: "Fashion",
    categoryId: "fashion",
    rating: "4.4"
  },
  {
    id: "product-6",
    name: "Bluetooth Speaker",
    price: "$59.99",
    category: "Electronics",
    categoryId: "electronics",
    rating: "4.7"
  },
  {
    id: "product-7",
    name: "Leather Wallet",
    price: "$34.99",
    category: "Fashion",
    categoryId: "fashion",
    rating: "4.5"
  },
  {
    id: "product-8",
    name: "Ceramic Coffee Mug",
    price: "$14.99",
    category: "Home & Living",
    categoryId: "home",
    rating: "4.3"
  },
  {
    id: "product-9",
    name: "Face Care Set",
    price: "$29.99",
    category: "Beauty",
    categoryId: "beauty",
    rating: "4.6"
  },
  {
    id: "product-10",
    name: "Moisturizing Cream",
    price: "$19.99",
    category: "Beauty",
    categoryId: "beauty",
    rating: "4.5"
  }
];

function CategoryProducts() {
  const { id } = useParams();

  const category = categories.find(
    (item) => item.id === id
  );

  if (!category) {
    return (
      <section className="page">
        <div className="container">
          <h1 className="page-title">
            Category Not Found
          </h1>

          <p className="page-description">
            We couldn't find the category you're looking for.
          </p>

          <Link
            className="button"
            to="/categories"
          >
            Back to Categories
          </Link>
        </div>
      </section>
    );
  }

  const categoryProducts = products.filter(
    (product) => product.categoryId === category.id
  );

  return (
    <section className="category-products-page">
      <div className="container">
        <div className="category-products-header">
          <div>
            <Link
              className="category-products-back-link"
              to="/categories"
            >
              ← Back to Categories
            </Link>

            <p className="category-products-eyebrow">
              Category
            </p>

            <h1 className="page-title">
              {category.name}
            </h1>

            <p className="category-products-description">
              {category.description}
            </p>
          </div>

          <p className="category-products-count">
            {categoryProducts.length}{" "}
            {categoryProducts.length === 1
              ? "product"
              : "products"}
          </p>
        </div>

        <ProductGrid products={categoryProducts} />
      </div>
    </section>
  );
}

export default CategoryProducts;