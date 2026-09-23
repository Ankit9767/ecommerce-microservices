import React from "react";
import { Outlet } from "react-router-dom";

import Header from "./Header";

function Layout() {
  return (
    <div className="app">
      <Header />

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