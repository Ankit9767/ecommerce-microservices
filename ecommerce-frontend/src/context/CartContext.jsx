import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from "react";

import {
  addCartItem,
  clearCart,
  getCart,
  removeCartItem,
  updateCartItem
} from "../services/cartService";

import { useAuth } from "./AuthContext";

const CartContext = createContext(null);

function CartProvider({ children }) {
  const { isAuthenticated, isLoading: isAuthLoading } =
    useAuth();

  const [cart, setCart] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [error, setError] = useState("");

  const loadCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(null);
      setError("");
      return null;
    }

    setIsLoading(true);
    setError("");

    try {
      const response = await getCart();

      setCart(response);

      return response;
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to load your cart."
      );

      throw requestError;
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (isAuthLoading) {
      return;
    }

    if (!isAuthenticated) {
      setCart(null);
      setError("");
      return;
    }

    loadCart().catch(() => {
      // Error is already stored in context state.
    });
  }, [
    isAuthenticated,
    isAuthLoading,
    loadCart
  ]);

  const addToCart = async (product, quantity = 1) => {
    if (!product?.id) {
      throw new Error(
        "A valid product is required to add an item to the cart."
      );
    }

    setIsUpdating(true);
    setError("");

    try {
      const response = await addCartItem(
        product.id,
        quantity
      );

      setCart(response);

      return response;
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to add the product to your cart."
      );

      throw requestError;
    } finally {
      setIsUpdating(false);
    }
  };

  const updateQuantity = async (
    productId,
    quantity
  ) => {
    if (!productId) {
      throw new Error(
        "A valid product ID is required."
      );
    }

    setIsUpdating(true);
    setError("");

    try {
      const response = await updateCartItem(
        productId,
        quantity
      );

      setCart(response);

      return response;
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to update the cart item."
      );

      throw requestError;
    } finally {
      setIsUpdating(false);
    }
  };

  const removeFromCart = async (productId) => {
    if (!productId) {
      throw new Error(
        "A valid product ID is required."
      );
    }

    setIsUpdating(true);
    setError("");

    try {
      const response =
        await removeCartItem(productId);

      setCart(response);

      return response;
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to remove the cart item."
      );

      throw requestError;
    } finally {
      setIsUpdating(false);
    }
  };

  const emptyCart = async () => {
    setIsUpdating(true);
    setError("");

    try {
      const response = await clearCart();

      setCart(response);

      return response;
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to clear your cart."
      );

      throw requestError;
    } finally {
      setIsUpdating(false);
    }
  };

  const value = useMemo(
    () => ({
      cart,
      items: cart?.items || [],
      totalAmount: cart?.totalAmount || 0,
      totalItems: cart?.totalItems || 0,
      isLoading,
      isUpdating,
      error,
      loadCart,
      addToCart,
      updateQuantity,
      removeFromCart,
      emptyCart
    }),
    [
      cart,
      isLoading,
      isUpdating,
      error,
      loadCart
    ]
  );

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
}

function useCart() {
  const context = useContext(CartContext);

  if (!context) {
    throw new Error(
      "useCart must be used inside CartProvider"
    );
  }

  return context;
}

export {
  CartProvider,
  useCart
};