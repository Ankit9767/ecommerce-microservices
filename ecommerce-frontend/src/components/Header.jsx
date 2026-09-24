import React from "react";
import { Link, NavLink } from "react-router-dom";

import { useCart } from "../context/CartContext";

import "./styles/Header.css";

function Header() {
  const { cartItemCount } = useCart();

  return (
    <header className="site-header">
      <div className="container header-content">
        <Link className="brand" to="/">
          EcommerceHub
        </Link>

        <nav
          className="main-navigation"
          aria-label="Main navigation"
        >
          <NavLink
            to="/"
            className={({ isActive }) =>
              isActive
                ? "nav-link active"
                : "nav-link"
            }
          >
            Home
          </NavLink>

          <NavLink
            to="/products"
            className={({ isActive }) =>
              isActive
                ? "nav-link active"
                : "nav-link"
            }
          >
            Products
          </NavLink>

          <NavLink
            to="/categories"
            className={({ isActive }) =>
              isActive
                ? "nav-link active"
                : "nav-link"
            }
          >
            Categories
          </NavLink>

          <NavLink
            to="/cart"
            className={({ isActive }) =>
              isActive
                ? "nav-link active cart-nav-link"
                : "nav-link cart-nav-link"
            }
          >
            Cart

            {cartItemCount > 0 && (
              <span className="cart-count">
                {cartItemCount}
              </span>
            )}
          </NavLink>

          <NavLink
            to="/login"
            className={({ isActive }) =>
              isActive
                ? "nav-link active"
                : "nav-link"
            }
          >
            Login
          </NavLink>
        </nav>
      </div>
    </header>
  );
}

export default Header;