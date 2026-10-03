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
  const {
    isAuthenticated,
    isLoading: isAuthLoading
  } = useAuth();

  const [cart, setCart] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // Keeps track of whether ANY cart operation is running.
  const [isUpdating, setIsUpdating] = useState(false);

  // Keeps track of WHICH product is currently being added.
  const [addingProductId, setAddingProductId] = useState(null);

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
      setAddingProductId(null);
      return;
    }

    loadCart().catch(() => {});
  }, [
    isAuthenticated,
    isAuthLoading,
    loadCart
  ]);

  const addToCart = useCallback(
    async (product, quantity = 1) => {
      if (!product?.id) {
        throw new Error(
          "A valid product is required to add an item to the cart."
        );
      }

      // Store the exact product being added.
      setAddingProductId(product.id);

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
        // Clear the product-specific loading state.
        setAddingProductId(null);

        setIsUpdating(false);
      }
    },
    []
  );

  const updateQuantity = useCallback(
    async (productId, quantity) => {
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
    },
    []
  );

  const removeFromCart = useCallback(
    async (productId) => {
      if (!productId) {
        throw new Error(
          "A valid product ID is required."
        );
      }

      setIsUpdating(true);
      setError("");

      try {
        const response = await removeCartItem(productId);

        setCart(response);

        return response;
      } catch (requestError) {
        setError(
          requestError.message ||
            "Unable to remove the product from your cart."
        );

        throw requestError;
      } finally {
        setIsUpdating(false);
      }
    },
    []
  );

  const emptyCart = useCallback(async () => {
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
  }, []);

  /*
   * cart.totalItems is the backend-provided total item count.
   *
   * Keep both names:
   * - totalItems for cart pages
   * - cartItemCount for the header
   */
  const totalItems = Number(
    cart?.totalItems || 0
  );

  const cartItemCount = totalItems;

  const value = useMemo(
    () => ({
      cart,
      items: cart?.items || [],
      totalAmount: cart?.totalAmount || 0,

      totalItems,
      cartItemCount,

      isLoading,

      // Global cart operation state.
      isUpdating,

      // Product-specific add state.
      addingProductId,

      error,

      loadCart,
      addToCart,
      updateQuantity,
      removeFromCart,
      emptyCart
    }),
    [
      cart,
      totalItems,
      cartItemCount,
      isLoading,
      isUpdating,
      addingProductId,
      error,
      loadCart,
      addToCart,
      updateQuantity,
      removeFromCart,
      emptyCart
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
