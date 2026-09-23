import React from "react";
import { useParams } from "react-router-dom";

function ProductDetails() {
  const { id } = useParams();

  return (
    <section className="page">
      <div className="container">
        <h1 className="page-title">Product Details</h1>

        <p className="page-description">
          Product ID: {id}
        </p>
      </div>
    </section>
  );
}

export default ProductDetails;