import {
  apiGet,
  apiPost,
  apiPut,
  apiDelete
} from "./api";

function getCategories() {
  return apiGet("/categories");
}

function getCategory(categoryId) {
  return apiGet(`/categories/${categoryId}`);
}

function createCategory(request) {
  return apiPost("/categories", request);
}

function updateCategory(categoryId, request) {
  return apiPut(
    `/categories/${categoryId}`,
    request
  );
}

function deleteCategory(categoryId) {
  return apiDelete(`/categories/${categoryId}`);
}

export {
  getCategories,
  getCategory,
  createCategory,
  updateCategory,
  deleteCategory
};