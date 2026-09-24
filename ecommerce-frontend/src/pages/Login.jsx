import React, { useState } from "react";
import {
  Link,
  useLocation,
  useNavigate
} from "react-router-dom";

import FormField from "../components/FormField";
import { useAuth } from "../context/AuthContext";

import "./styles/Login.css";

function Login() {
  const navigate = useNavigate();
  const location = useLocation();

  const { login } = useAuth();

  const [form, setForm] = useState({
    usernameOrEmail: "",
    password: ""
  });

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const redirectPath =
    location.state?.from?.pathname || "/";

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((currentForm) => ({
      ...currentForm,
      [name]: value
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError("");
    setIsSubmitting(true);

    try {
      await login(form);
      navigate(redirectPath, {
        replace: true
      });
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to log in. Please try again."
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="page login-page">
      <div className="container">
        <div className="login-card">
          <h1 className="page-title">
            Login
          </h1>

          <p className="page-description">
            Sign in to your EcommerceHub account.
          </p>

          {error && (
            <p
              className="login-error"
              role="alert"
            >
              {error}
            </p>
          )}

          <form
            className="login-form"
            onSubmit={handleSubmit}
          >
            <FormField
              id="usernameOrEmail"
              name="usernameOrEmail"
              label="Username or Email"
              value={form.usernameOrEmail}
              onChange={handleChange}
              required
              autoComplete="username"
            />

            <FormField
              id="password"
              name="password"
              label="Password"
              type="password"
              value={form.password}
              onChange={handleChange}
              required
              autoComplete="current-password"
            />

            <button
              type="submit"
              className="button"
              disabled={isSubmitting}
            >
              {isSubmitting
                ? "Signing in..."
                : "Login"}
            </button>
          </form>

          <p className="login-register-link">
            Don't have an account?{" "}
            <Link to="/register">
              Register
            </Link>
          </p>
        </div>
      </div>
    </section>
  );
}

export default Login;