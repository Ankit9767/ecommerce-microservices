import React, { useState } from "react";
import { Link } from "react-router-dom";

import FormField from "../components/FormField";

import "./styles/Login.css";

function Login() {
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });

  const [errors, setErrors] = useState({});

  const [showPassword, setShowPassword] = useState(false);

  const [submitted, setSubmitted] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((currentData) => ({
      ...currentData,
      [name]: value
    }));

    setErrors((currentErrors) => ({
      ...currentErrors,
      [name]: ""
    }));

    setSubmitted(false);
  };

  const validateForm = () => {
    const validationErrors = {};

    if (!formData.email.trim()) {
      validationErrors.email =
        "Email address is required.";
    } else if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
        formData.email
      )
    ) {
      validationErrors.email =
        "Enter a valid email address.";
    }

    if (!formData.password) {
      validationErrors.password =
        "Password is required.";
    }

    return validationErrors;
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    const validationErrors = validateForm();

    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setSubmitted(true);
  };

  return (
    <section className="auth-page">
      <div className="container">
        <div className="auth-card">
          <div className="auth-header">
            <p className="auth-eyebrow">
              Welcome back
            </p>

            <h1 className="auth-title">
              Login
            </h1>

            <p className="auth-description">
              Sign in to your EcommerceHub account.
            </p>
          </div>

          <form
            className="auth-form"
            onSubmit={handleSubmit}
            noValidate
          >
            <FormField
              id="login-email"
              label="Email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="you@example.com"
              required
              error={errors.email}
              autoComplete="email"
            />

            <div className="auth-password-field">
              <FormField
                id="login-password"
                label="Password"
                type={
                  showPassword
                    ? "text"
                    : "password"
                }
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Enter your password"
                required
                error={errors.password}
                autoComplete="current-password"
              />

              <button
                type="button"
                className="auth-password-toggle"
                onClick={() =>
                  setShowPassword(
                    (currentValue) => !currentValue
                  )
                }
              >
                {showPassword
                  ? "Hide password"
                  : "Show password"}
              </button>
            </div>

            <button
              type="submit"
              className="button auth-submit-button"
            >
              Login
            </button>

            {submitted && (
              <p
                className="auth-success"
                role="status"
              >
                Login form submitted successfully.
                Authentication will be connected to
                the backend later.
              </p>
            )}
          </form>

          <p className="auth-footer-text">
            Don't have an account?{" "}
            <Link to="/register">
              Create an account
            </Link>
          </p>
        </div>
      </div>
    </section>
  );
}

export default Login;