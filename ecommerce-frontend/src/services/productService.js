import {
  apiGet,
  apiPost,
  apiPut,
  apiDelete,
  apiRequest
} from "./api";

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

  if (minPrice !== undefined && minPrice !== "") {
    params.set("minPrice", minPrice);
  }

  if (maxPrice !== undefined && maxPrice !== "") {
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

function getProduct(productId) {
  return apiGet(`/products/${productId}`);
}

function createProduct(request) {
  return apiPost("/products", request);
}

function updateProduct(productId, request) {
  return apiPut(`/products/${productId}`, request);
}

function deleteProduct(productId) {
  return apiDelete(`/products/${productId}`);
}

function getProductImages(productId) {
  return apiGet(`/products/${productId}/images`);
}

function getProductImage(productId, imageId) {
  return apiGet(
    `/products/${productId}/images/${imageId}`
  );
}

function getProductImageContentUrl(
  productId,
  imageId
) {
  return `/api/products/${productId}/images/${imageId}/content`;
}

function uploadProductImage(
  productId,
  file,
  {
    displayOrder = 0,
    primaryImage = false
  } = {}
) {
  const formData = new FormData();

  formData.append("file", file);
  formData.append(
    "displayOrder",
    displayOrder
  );
  formData.append(
    "primaryImage",
    primaryImage
  );

  return apiRequest(
    `/products/${productId}/images`,
    {
      method: "POST",
      body: formData,
      isFormData: true
    }
  );
}

function setPrimaryProductImage(
  productId,
  imageId
) {
  return apiPut(
    `/products/${productId}/images/${imageId}/primary`
  );
}

function deleteProductImage(
  productId,
  imageId
) {
  return apiDelete(
    `/products/${productId}/images/${imageId}`
  );
}

export {
  searchProducts,
  getProduct,
  createProduct,
  updateProduct,
  deleteProduct,
  getProductImages,
  getProductImage,
  getProductImageContentUrl,
  uploadProductImage,
  setPrimaryProductImage,
  deleteProductImage
};