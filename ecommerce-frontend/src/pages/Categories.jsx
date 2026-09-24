import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { getCategories } from "../services/categoryService";

import "./styles/Categories.css";

function Categories() {
  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadCategories() {
      setIsLoading(true);
      setError("");

      try {
        const response = await getCategories();

        if (!isMounted) {
          return;
        }

        const categoryList = Array.isArray(response)
          ? response
          : response?.content || [];

        const activeCategories = categoryList.filter(
          (category) => category.active !== false
        );

        setCategories(activeCategories);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setCategories([]);
        setError(
          requestError.message ||
            "Unable to load categories. Please try again."
        );
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadCategories();

    return () => {
      isMounted = false;
    };
  }, []);

  if (isLoading) {
    return (
      <section className="page categories-page">
        <div className="container">
          <div className="categories-status">
            <p>Loading categories...</p>
          </div>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="page categories-page">
        <div className="container">
          <div
            className="categories-status categories-status-error"
            role="alert"
          >
            <p>{error}</p>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="page categories-page">
      <div className="container">
        <header className="categories-header">
          <p className="categories-eyebrow">
            EcommerceHub
          </p>

          <h1 className="page-title">
            Categories
          </h1>

          <p className="page-description">
            Browse products by category.
          </p>
        </header>

        {categories.length === 0 ? (
          <div className="categories-status">
            <p>No categories are currently available.</p>
          </div>
        ) : (
          <div className="categories-grid">
            {categories.map((category) => (
              <article
                className="category-card"
                key={category.id}
              >
                <div className="category-card-content">
                  <h2 className="category-card-title">
                    {category.name}
                  </h2>

                  {category.description && (
                    <p className="category-card-description">
                      {category.description}
                    </p>
                  )}

                  <Link
                    className="button category-card-link"
                    to={`/categories/${category.id}`}
                  >
                    View Products
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export default Categories;