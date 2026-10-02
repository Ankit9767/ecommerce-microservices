
import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { getCategories } from "../services/categoryService";

import "./styles/Categories.css";

function CategoryIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="categories-icon"
    >
      <rect x="4" y="4" width="6" height="6" rx="1" />
      <rect x="14" y="4" width="6" height="6" rx="1" />
      <rect x="4" y="14" width="6" height="6" rx="1" />
      <rect x="14" y="14" width="6" height="6" rx="1" />
    </svg>
  );
}

function GridIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="categories-icon"
    >
      <rect x="3.5" y="3.5" width="7" height="7" rx="1.5" />
      <rect x="13.5" y="3.5" width="7" height="7" rx="1.5" />
      <rect x="3.5" y="13.5" width="7" height="7" rx="1.5" />
      <rect x="13.5" y="13.5" width="7" height="7" rx="1.5" />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="categories-icon"
    >
      <path d="M5 12h14" />
      <path d="m13 6 6 6-6 6" />
    </svg>
  );
}

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
          <div className="categories-loading">
            <div className="categories-loading-icon">
              <CategoryIcon />
            </div>

            <div className="categories-loading-text">
              <span />
              <span />
            </div>

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
            <div className="categories-status-error-icon">
              !
            </div>

            <div>
              <h2>Unable to load categories</h2>
              <p>{error}</p>
            </div>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="page categories-page">
      <div className="container">
        <header className="categories-header">
          <div className="categories-header-content">
            <div className="categories-title-row">
              <div className="categories-title-icon">
                <CategoryIcon />
              </div>

              <div>
                <p className="categories-eyebrow">
                  EcommerceHub
                </p>

                <h1 className="page-title">
                  Categories
                </h1>
              </div>
            </div>

            <p className="categories-description">
              Explore our collection through thoughtfully
              organized categories and discover products
              that fit what you need.
            </p>

            <div className="categories-header-meta">
              <span className="categories-meta-item">
                <GridIcon />
                Browse categories
              </span>

              <span className="categories-meta-divider" />

              <span className="categories-meta-item">
                <CategoryIcon />
                Explore products
              </span>
            </div>
          </div>
        </header>

        {categories.length === 0 ? (
          <div className="categories-empty">
            <div className="categories-empty-icon">
              <CategoryIcon />
            </div>

            <h2>No categories available</h2>

            <p>
              No categories are currently available.
            </p>
          </div>
        ) : (
          <div className="categories-grid">
            {categories.map((category, index) => (
              <Link
                className="category-card"
                key={category.id}
                to={`/categories/${category.id}`}
                style={{
                  "--category-delay": `${index * 75}ms`
                }}
              >
                <div className="category-card-background">
                  <span />
                  <span />
                </div>

                <div className="category-card-main">
                  <div className="category-card-icon">
                    <CategoryIcon />
                  </div>

                  <div className="category-card-title-wrap">
                    <h2 className="category-card-title">
                      {category.name}
                    </h2>

                    {category.description && (
                      <p className="category-card-description">
                        {category.description}
                      </p>
                    )}
                  </div>
                </div>

                <div className="category-card-footer">
                  <span>Explore Products</span>

                  <ArrowIcon />
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export default Categories;

