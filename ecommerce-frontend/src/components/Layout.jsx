import React from "react";
import { Link, Outlet } from "react-router-dom";

function Layout() {
  return (
    <div className="app">
      <header className="site-header">
        <div className="container header-content">
          <Link className="brand" to="/">
            EcommerceHub
          </Link>

          <nav className="main-navigation" aria-label="Main navigation">
            <Link to="/">Home</Link>
          </nav>
        </div>
      </header>

      <main className="site-main">
        <Outlet />
      </main>

      <footer className="site-footer">
        <div className="container">
          <p>© 2026 EcommerceHub. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}

export default Layout;