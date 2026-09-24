const API_BASE_URL = "http://ecommercehub.com/api";

async function apiRequest(
  path,
  {
    method = "GET",
    body,
    token,
    headers = {},
    isFormData = false
  } = {}
) {
  const requestHeaders = {
    ...headers
  };

  if (!isFormData && body !== undefined) {
    requestHeaders["Content-Type"] = "application/json";
  }

  if (token) {
    requestHeaders.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(
    `${API_BASE_URL}${path}`,
    {
      method,
      headers: requestHeaders,
      body: isFormData
        ? body
        : body !== undefined
          ? JSON.stringify(body)
          : undefined
    }
  );

  if (!response.ok) {
    let errorData = null;

    try {
      errorData = await response.json();
    } catch {
      errorData = null;
    }

    const error = new Error(
      errorData?.message ||
        errorData?.error ||
        `Request failed with status ${response.status}`
    );

    error.status = response.status;
    error.data = errorData;

    throw error;
  }

  if (response.status === 204) {
    return null;
  }

  const contentType =
    response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    return response.json();
  }

  return response.text();
}

function getAccessToken() {
  return localStorage.getItem("accessToken");
}

function apiGet(path, options = {}) {
  return apiRequest(path, {
    ...options,
    method: "GET",
    token:
      options.token !== undefined
        ? options.token
        : getAccessToken()
  });
}

function apiPost(path, body, options = {}) {
  return apiRequest(path, {
    ...options,
    method: "POST",
    body,
    token:
      options.token !== undefined
        ? options.token
        : getAccessToken()
  });
}

function apiPut(path, body, options = {}) {
  return apiRequest(path, {
    ...options,
    method: "PUT",
    body,
    token:
      options.token !== undefined
        ? options.token
        : getAccessToken()
  });
}

function apiPatch(path, body, options = {}) {
  return apiRequest(path, {
    ...options,
    method: "PATCH",
    body,
    token:
      options.token !== undefined
        ? options.token
        : getAccessToken()
  });
}

function apiDelete(path, options = {}) {
  return apiRequest(path, {
    ...options,
    method: "DELETE",
    token:
      options.token !== undefined
        ? options.token
        : getAccessToken()
  });
}

export {
  apiRequest,
  apiGet,
  apiPost,
  apiPut,
  apiPatch,
  apiDelete,
  getAccessToken
};