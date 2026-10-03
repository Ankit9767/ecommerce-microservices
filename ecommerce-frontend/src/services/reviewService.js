import { apiGet, apiPost, apiPatch, apiDelete } from "./api";

function createReview({ productId, orderId, rating, title, comment }) {
  return apiPost("/reviews", {
    productId,
    orderId,
    rating,
    title,
    comment,
  });
}

function getReview(reviewId) {
  return apiGet(`/reviews/${reviewId}`);
}

function getProductReviews(productId, { page = 0, size = 20, sort } = {}) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(`/reviews/product/${productId}?${params.toString()}`);
}

function getMyReviews({ page = 0, size = 20, sort } = {}) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(`/reviews/me?${params.toString()}`);
}

function updateReview(reviewId, { rating, title, comment }) {
  return apiPatch(`/reviews/${reviewId}`, {
    rating,
    title,
    comment,
  });
}

function deleteReview(reviewId) {
  return apiDelete(`/reviews/${reviewId}`);
}

export {
  createReview,
  getReview,
  getProductReviews,
  getMyReviews,
  updateReview,
  deleteReview,
};
