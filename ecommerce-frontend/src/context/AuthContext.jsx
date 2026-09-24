import React, {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState
} from "react";

import {
  getAccessToken,
  login as loginRequest,
  register as registerRequest,
  logout as logoutRequest
} from "../services/authService";

const AuthContext = createContext(null);

function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(
    () => getAccessToken()
  );

  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    setIsLoading(false);
  }, []);

  const login = async (credentials) => {
    const response = await loginRequest(credentials);

    localStorage.setItem(
      "accessToken",
      response.accessToken
    );

    if (response.refreshToken) {
      localStorage.setItem(
        "refreshToken",
        response.refreshToken
      );
    }

    setAccessToken(response.accessToken);

    return response;
  };

  const register = async (request) => {
    return registerRequest(request);
  };

  const logout = async () => {
    const refreshToken =
      localStorage.getItem("refreshToken");

    try {
      await logoutRequest(refreshToken);
    } finally {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      setAccessToken(null);
    }
  };

  const value = useMemo(
    () => ({
      accessToken,
      isAuthenticated: Boolean(accessToken),
      isLoading,
      login,
      register,
      logout
    }),
    [
      accessToken,
      isLoading
    ]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error(
      "useAuth must be used inside AuthProvider"
    );
  }

  return context;
}

export {
  AuthProvider,
  useAuth
};