import { apiGet } from "./api";

function searchProducts({
  q,
  category,
  minPrice,
  maxPrice,
  active,
  page = 0,
  size = 20,
  sort
} = {}) {
  const params = new URLSearchParams();

  if (q) {
    params.set("q", q);
  }

  if (category) {
    params.set("category", category);
  }

  if (
    minPrice !== undefined &&
    minPrice !== ""
  ) {
    params.set("minPrice", minPrice);
  }

  if (
    maxPrice !== undefined &&
    maxPrice !== ""
  ) {
    params.set("maxPrice", maxPrice);
  }

  if (active !== undefined) {
    params.set("active", active);
  }

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(
    `/search/products?${params.toString()}`
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
  searchProducts,
  searchProductReviews
};