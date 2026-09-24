import React from "react";
import { Link } from "react-router-dom";

import "./styles/CategoryCard.css";

function CategoryCard({ category }) {
  return (
    <Link
      className="category-card"
      to={`/categories/${category.id}`}
    >
      <div className="category-card-icon">
        {category.name.charAt(0)}
      </div>

      <div className="category-card-content">
        <h2 className="category-card-title">
          {category.name}
        </h2>

        {category.description && (
          <p className="category-card-description">
            {category.description}
          </p>
        )}

        <span className="category-card-link">
          View Products →
        </span>
      </div>
    </Link>
  );
}

export default CategoryCard;