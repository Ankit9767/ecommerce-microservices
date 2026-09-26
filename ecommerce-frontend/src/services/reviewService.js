import {
  apiGet,
  apiPost,
  apiPatch,
  apiDelete
} from "./api";

function createReview(request) {
  return apiPost(
    "/reviews",
    request
  );
}

function getReview(reviewId) {
  return apiGet(
    `/reviews/${reviewId}`
  );
}

function getProductReviews(
  productId,
  {
    page = 0,
    size = 20,
    sort
  } = {}
) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(
    `/reviews/product/${productId}?${params.toString()}`
  );
}

function getMyReviews({
  page = 0,
  size = 20,
  sort
} = {}) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(
    `/reviews/me?${params.toString()}`
  );
}

function updateReview(
  reviewId,
  request
) {
  return apiPatch(
    `/reviews/${reviewId}`,
    request
  );
}

function deleteReview(reviewId) {
  return apiDelete(
    `/reviews/${reviewId}`
  );
}

function searchProductReviews(
  productId,
  {
    page = 0,
    size = 20,
    sort
  } = {}
) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(
    `/search/reviews/product/${productId}?${params.toString()}`
  );
}

export {
  createReview,
  getReview,
  getProductReviews,
  getMyReviews,
  updateReview,
  deleteReview,
  searchProductReviews
};