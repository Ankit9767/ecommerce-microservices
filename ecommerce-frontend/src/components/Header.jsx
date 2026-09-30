import React, { useState } from "react";
import {
  Link,
  NavLink,
  useNavigate
} from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

import "./styles/Header.css";

function Header() {
  const navigate = useNavigate();

  const {
    isAuthenticated,
    logout
  } = useAuth();

  const { cartItemCount } = useCart();

  const [isLoggingOut, setIsLoggingOut] =
    useState(false);

  const [logoutError, setLogoutError] =
    useState("");

  const handleLogout = async () => {
    if (isLoggingOut) {
      return;
    }

    setIsLoggingOut(true);
    setLogoutError("");

    try {
      await logout();

      navigate("/", {
        replace: true
      });
    } catch (requestError) {
      setLogoutError(
        requestError.message ||
          "Unable to log out. Please try again."
      );
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <header className="site-header">
      <div className="container header-content">
        <Link
          className="brand"
          to="/"
        >
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

          {isAuthenticated && (
            <NavLink
              to="/orders"
              className={({ isActive }) =>
                isActive
                  ? "nav-link active"
                  : "nav-link"
              }
            >
              Orders
            </NavLink>
          )}

          <NavLink
            to="/cart"
            className={({ isActive }) =>
              isActive
                ? "nav-link active cart-nav-link"
                : "nav-link cart-nav-link"
            }
          >
            <span>Cart</span>

            {cartItemCount > 0 && (
              <span
                className="cart-count"
                aria-label={`${cartItemCount} items in cart`}
              >
                {cartItemCount}
              </span>
            )}
          </NavLink>

          {!isAuthenticated && (
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
          )}

          {isAuthenticated && (
            <button
              type="button"
              className="nav-link logout-button"
              onClick={handleLogout}
              disabled={isLoggingOut}
              aria-busy={isLoggingOut}
            >
              {isLoggingOut
                ? "Logging out..."
                : "Logout"}
            </button>
          )}
        </nav>
      </div>

      {logoutError && (
        <div
          className="container header-error"
          role="alert"
        >
          {logoutError}
        </div>
      )}
    </header>
  );
}

export default Header;