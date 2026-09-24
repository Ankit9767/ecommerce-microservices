import {
  apiGet,
  apiPost,
  apiDelete
} from "./api";

function register({
  username,
  firstName,
  lastName,
  email,
  password,
  phone
}) {
  return apiPost(
    "/auth/register",
    {
      username,
      firstName,
      lastName,
      email,
      password,
      phone
    },
    {
      token: null
    }
  );
}

function login({
  usernameOrEmail,
  password
}) {
  return apiPost(
    "/auth/login",
    {
      usernameOrEmail,
      password
    },
    {
      token: null
    }
  );
}

function refreshToken(refreshTokenValue) {
  return apiPost(
    "/auth/refresh",
    {
      refreshToken: refreshTokenValue
    },
    {
      token: null
    }
  );
}

function logout(refreshTokenValue) {
  return apiPost(
    "/auth/logout",
    {
      refreshToken: refreshTokenValue
    }
  );
}

function forgotPassword(email) {
  return apiPost(
    "/auth/forgot-password",
    {
      email
    },
    {
      token: null
    }
  );
}

function resetPassword({
  token,
  newPassword,
  confirmPassword
}) {
  return apiPost(
    "/auth/reset-password",
    {
      token,
      newPassword,
      confirmPassword
    },
    {
      token: null
    }
  );
}

function getSessions() {
  return apiGet("/auth/sessions");
}

function logoutSession(sessionId) {
  return apiDelete(
    `/auth/sessions/${sessionId}`
  );
}

function logoutAllSessions() {
  return apiDelete("/auth/sessions");
}

export {
  register,
  login,
  refreshToken,
  logout,
  forgotPassword,
  resetPassword,
  getSessions,
  logoutSession,
  logoutAllSessions
};