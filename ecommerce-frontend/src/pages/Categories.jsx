import React, {
  useEffect,
  useState
} from "react";

import CategoryCard from "../components/CategoryCard";
import { getCategories } from "../services/categoryService";

import "./styles/Categories.css";

function Categories() {
  const [categories, setCategories] =
    useState([]);

  const [isLoading, setIsLoading] =
    useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadCategories() {
      setIsLoading(true);
      setError("");

      try {
        const response =
          await getCategories();

        if (isMounted) {
          setCategories(response || []);
        }
      } catch (requestError) {
        if (isMounted) {
          setError(
            requestError.message ||
              "Unable to load categories."
          );
        }
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

  return (
    <section className="page categories-page">
      <div className="container">
        <h1 className="page-title">
          Categories
        </h1>

        <p className="page-description">
          Browse products by category.
        </p>

        {isLoading && (
          <p>Loading categories...</p>
        )}

        {!isLoading && error && (
          <p
            className="categories-error"
            role="alert"
          >
            {error}
          </p>
        )}

        {!isLoading &&
          !error &&
          categories.length === 0 && (
            <p>No categories found.</p>
          )}

        {!isLoading && !error && (
          <div className="categories-grid">
            {categories.map((category) => (
              <CategoryCard
                key={category.id}
                category={category}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export default Categories;