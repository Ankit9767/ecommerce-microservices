import React from "react";
import { useParams } from "react-router-dom";

function CategoryProducts() {
  const { id } = useParams();

  return (
    <section className="page">
      <div className="container">
        <h1 className="page-title">Category Products</h1>

        <p className="page-description">
          Category ID: {id}
        </p>
      </div>
    </section>
  );
}

export default CategoryProducts;