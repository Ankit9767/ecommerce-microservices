import {
  apiGet,
  apiPost,
  apiPut,
  apiDelete
} from "./api";

function getCart() {
  return apiGet("/cart");
}

function addCartItem(productId, quantity) {
  return apiPost("/cart/items", {
    productId,
    quantity
  });
}

function updateCartItem(productId, quantity) {
  return apiPut(`/cart/items/${productId}`, {
    quantity
  });
}

function removeCartItem(productId) {
  return apiDelete(`/cart/items/${productId}`);
}

function clearCart() {
  return apiDelete("/cart");
}

function getAllCarts({
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

  return apiGet(`/cart/all?${params.toString()}`);
}

export {
  getCart,
  addCartItem,
  updateCartItem,
  removeCartItem,
  clearCart,
  getAllCarts
};