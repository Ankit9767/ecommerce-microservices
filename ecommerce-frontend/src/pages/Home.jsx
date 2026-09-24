import React from "react";
import { Link } from "react-router-dom";
import ProductGrid from "../components/ProductGrid";

import "./styles/Home.css";

const featuredCategories = [
  {
    id: "electronics",
    name: "Electronics",
    description: "Latest devices and everyday technology."
  },
  {
    id: "fashion",
    name: "Fashion",
    description: "Styles for every occasion."
  },
  {
    id: "home",
    name: "Home & Living",
    description: "Make your space feel like home."
  },
  {
    id: "beauty",
    name: "Beauty",
    description: "Personal care and beauty essentials."
  }
];

const featuredProducts = [
  {
    id: "product-1",
    name: "Wireless Headphones",
    price: 79.99,
    category: "Electronics"
  },
  {
    id: "product-2",
    name: "Classic Sneakers",
    price: 64.99,
    category: "Fashion"
  },
  {
    id: "product-3",
    name: "Smart Watch",
    price: 129.99,
    category: "Electronics"
  },
  {
    id: "product-4",
    name: "Minimal Desk Lamp",
    price: 39.99,
    category: "Home & Living"
  }
];

function Home() {
  return (
    <div className="home-page">
      <section className="home-hero">
        <div className="container">
          <div className="home-hero-content">
            <p className="home-hero-eyebrow">
              Welcome to EcommerceHub
            </p>

            <h1 className="home-hero-title">
              Discover products you'll love.
            </h1>

            <p className="home-hero-description">
              Explore quality products across electronics, fashion,
              home, beauty, and more.
            </p>

            <div className="home-hero-actions">
              <Link className="button" to="/products">
                Shop Products
              </Link>

              <Link className="home-secondary-button" to="/categories">
                Browse Categories
              </Link>
            </div>
          </div>
        </div>
      </section>

      <section className="home-section">
        <div className="container">
          <div className="home-section-header">
            <div>
              <p className="home-section-eyebrow">
                Explore
              </p>

              <h2 className="home-section-title">
                Shop by Category
              </h2>
            </div>

            <Link className="home-section-link" to="/categories">
              View all
            </Link>
          </div>

          <div className="category-grid">
            {featuredCategories.map((category) => (
              <Link
                key={category.id}
                className="category-card"
                to={`/categories/${category.id}`}
              >
                <div className="category-card-icon">
                  {category.name.charAt(0)}
                </div>

                <h3>{category.name}</h3>

                <p>{category.description}</p>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="home-section home-products-section">
        <div className="container">
          <div className="home-section-header">
            <div>
              <p className="home-section-eyebrow">
                Featured
              </p>

              <h2 className="home-section-title">
                Featured Products
              </h2>
            </div>

            <Link className="home-section-link" to="/products">
              View all
            </Link>
          </div>

          <ProductGrid products={featuredProducts} />
        </div>
      </section>

      <section className="home-promotion">
        <div className="container">
          <div className="home-promotion-content">
            <div>
              <p className="home-section-eyebrow">
                EcommerceHub
              </p>

              <h2>
                Find something great for every part of your life.
              </h2>

              <p>
                Browse our growing collection and discover products
                selected for everyday shopping.
              </p>
            </div>

            <Link className="button" to="/products">
              Start Shopping
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Home;