import React from "react";

import CategoryCard from "../components/CategoryCard";

import "./styles/Categories.css";

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

function Categories() {
  return (
    <section className="categories-page">
      <div className="container">
        <div className="categories-header">
          <p className="categories-eyebrow">
            Explore our collection
          </p>

          <h1 className="page-title">
            Categories
          </h1>

          <p className="categories-description">
            Browse products by category and find what you are
            looking for.
          </p>
        </div>

        <div className="categories-grid">
          {categories.map((category) => (
            <CategoryCard
              key={category.id}
              category={category}
            />
          ))}
        </div>
      </div>
    </section>
  );
}

export default Categories;