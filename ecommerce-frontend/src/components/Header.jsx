import React, { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

import ConfirmModal from "../components/ConfirmModal";

import "./styles/Header.css";

function HomeIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="nav-icon">
      <path d="M3 10.5 12 3l9 7.5" />
      <path d="M5 9.5V21h14V9.5" />
      <path d="M9.5 21v-7h5v7" />
    </svg>
  );
}

function ProductsIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="nav-icon">
      <rect x="4" y="4" width="16" height="16" rx="2" />
      <path d="M4 9h16" />
      <path d="M9 9v11" />
    </svg>
  );
}

function CategoriesIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="nav-icon">
      <rect x="4" y="4" width="6" height="6" rx="1" />
      <rect x="14" y="4" width="6" height="6" rx="1" />
      <rect x="4" y="14" width="6" height="6" rx="1" />
      <rect x="14" y="14" width="6" height="6" rx="1" />
    </svg>
  );
}

function PackageIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="nav-icon">
      <path d="M12 2 22 7v10l-10 5L2 17V7l10-5Z" />
      <path d="M2 7l10 5.5L22 7" />
      <path d="M12 12.5V22" />
    </svg>
  );
}

function CartIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="nav-icon">
      <path d="M3 4h2l2.2 11h10.9l2-8H6" />
      <circle cx="9" cy="19" r="1.5" />
      <circle cx="17" cy="19" r="1.5" />
    </svg>
  );
}

function LoginIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="nav-icon">
      <path d="M10 4H5v16h5" />
      <path d="M13 8l4 4-4 4" />
      <path d="M8 12h9" />
    </svg>
  );
}

function LogoutIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="nav-icon">
      <path d="M14 4h5v16h-5" />
      <path d="M11 8l-4 4 4 4" />
      <path d="M7 12h10" />
    </svg>
  );
}

function Header() {
  const navigate = useNavigate();

  const { isAuthenticated, logout } = useAuth();

  const { cartItemCount } = useCart();

  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);

  const [logoutError, setLogoutError] = useState("");

  const handleLogout = async () => {
    if (isLoggingOut) {
      return;
    }

    setIsLoggingOut(true);
    setLogoutError("");

    try {
      await logout();

      setIsLogoutModalOpen(false);

      navigate("/", {
        replace: true,
      });
    } catch (requestError) {
      setLogoutError(
        requestError.message || "Unable to log out. Please try again.",
      );
    } finally {
      setIsLoggingOut(false);
    }
  };

  const handleLogoutClick = () => {
    if (isLoggingOut) {
      return;
    }

    setLogoutError("");
    setIsLogoutModalOpen(true);
  };

  const handleCancelLogout = () => {
    if (isLoggingOut) {
      return;
    }

    setIsLogoutModalOpen(false);
  };

  return (
    <>
      <header className="site-header">
        <div className="container header-content">
          <Link className="brand" to="/" aria-label="EcommerceHub home">
            <span className="brand-mark">
              <svg
                viewBox="0 0 24 24"
                aria-hidden="true"
                className="brand-cart-icon"
              >
                <path d="M3 4h2l2.2 11h10.9l2-8H6" />
                <circle cx="9" cy="19" r="1.5" />
                <circle cx="17" cy="19" r="1.5" />
              </svg>
            </span>

            <span className="brand-text">EcommerceHub</span>
          </Link>
          <nav className="main-navigation" aria-label="Main navigation">
            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              <HomeIcon />
              <span>Home</span>
            </NavLink>

            <NavLink
              to="/products"
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              <ProductsIcon />
              <span>Products</span>
            </NavLink>

            <NavLink
              to="/categories"
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              <CategoriesIcon />
              <span>Categories</span>
            </NavLink>

            {isAuthenticated && (
              <NavLink
                to="/orders"
                className={({ isActive }) =>
                  isActive ? "nav-link active" : "nav-link"
                }
              >
                <PackageIcon />
                <span>Orders</span>
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
              <CartIcon />

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
                  isActive ? "nav-link active" : "nav-link"
                }
              >
                <LoginIcon />
                <span>Login</span>
              </NavLink>
            )}

            {isAuthenticated && (
              <button
                type="button"
                className="nav-link logout-button"
                onClick={handleLogoutClick}
                disabled={isLoggingOut}
                aria-busy={isLoggingOut}
              >
                <LogoutIcon />

                <span>{isLoggingOut ? "Logging out..." : "Logout"}</span>
              </button>
            )}
          </nav>
        </div>

        {logoutError && (
          <div className="container header-error" role="alert">
            {logoutError}
          </div>
        )}
      </header>
      <ConfirmModal
        isOpen={isLogoutModalOpen}
        title="Log out of EcommerceHub?"
        message="Are you sure you want to log out of your account?"
        confirmText="Log Out"
        cancelText="Cancel"
        onConfirm={handleLogout}
        onCancel={handleCancelLogout}
        isLoading={isLoggingOut}
        loadingText="Logging out..."
      />
    </>
  );
}

export default Header;
