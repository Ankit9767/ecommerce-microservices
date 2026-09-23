import React from "react";
import { Link } from "react-router-dom";

function NotFound() {
  return (
    <section className="not-found-page">
      <div className="container">
        <h1>404</h1>

        <p>The page you are looking for does not exist.</p>

        <Link className="button" to="/">
          Back to Home
        </Link>
      </div>
    </section>
  );
}

export default NotFound;