import React, { useState } from "react";
import { Link } from "react-router-dom";

import FormField from "../components/FormField";

import "./styles/Register.css";

function Register() {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: ""
  });

  const [errors, setErrors] = useState({});

  const [showPassword, setShowPassword] =
    useState(false);

  const [showConfirmPassword, setShowConfirmPassword] =
    useState(false);

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

    if (!formData.firstName.trim()) {
      validationErrors.firstName =
        "First name is required.";
    }

    if (!formData.lastName.trim()) {
      validationErrors.lastName =
        "Last name is required.";
    }

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
    } else if (formData.password.length < 8) {
      validationErrors.password =
        "Password must be at least 8 characters.";
    }

    if (!formData.confirmPassword) {
      validationErrors.confirmPassword =
        "Please confirm your password.";
    } else if (
      formData.password !==
      formData.confirmPassword
    ) {
      validationErrors.confirmPassword =
        "Passwords do not match.";
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
    <section className="auth-page register-page">
      <div className="container">
        <div className="auth-card">
          <div className="auth-header">
            <p className="auth-eyebrow">
              Join EcommerceHub
            </p>

            <h1 className="auth-title">
              Create Account
            </h1>

            <p className="auth-description">
              Create your account to start shopping.
            </p>
          </div>

          <form
            className="auth-form"
            onSubmit={handleSubmit}
            noValidate
          >
            <div className="register-name-fields">
              <FormField
                id="register-first-name"
                name="firstName"
                label="First Name"
                value={formData.firstName}
                onChange={handleChange}
                placeholder="John"
                required
                error={errors.firstName}
                autoComplete="given-name"
              />

              <FormField
                id="register-last-name"
                name="lastName"
                label="Last Name"
                value={formData.lastName}
                onChange={handleChange}
                placeholder="Doe"
                required
                error={errors.lastName}
                autoComplete="family-name"
              />
            </div>

            <FormField
              id="register-email"
              name="email"
              label="Email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="you@example.com"
              required
              error={errors.email}
              autoComplete="email"
            />

            <div className="auth-password-field">
              <FormField
                id="register-password"
                name="password"
                label="Password"
                type={
                  showPassword
                    ? "text"
                    : "password"
                }
                value={formData.password}
                onChange={handleChange}
                placeholder="At least 8 characters"
                required
                error={errors.password}
                autoComplete="new-password"
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

            <div className="auth-password-field">
              <FormField
                id="register-confirm-password"
                name="confirmPassword"
                label="Confirm Password"
                type={
                  showConfirmPassword
                    ? "text"
                    : "password"
                }
                value={
                  formData.confirmPassword
                }
                onChange={handleChange}
                placeholder="Enter your password again"
                required
                error={
                  errors.confirmPassword
                }
                autoComplete="new-password"
              />

              <button
                type="button"
                className="auth-password-toggle"
                onClick={() =>
                  setShowConfirmPassword(
                    (currentValue) =>
                      !currentValue
                  )
                }
              >
                {showConfirmPassword
                  ? "Hide password"
                  : "Show password"}
              </button>
            </div>

            <button
              type="submit"
              className="button auth-submit-button"
            >
              Create Account
            </button>

            {submitted && (
              <p
                className="auth-success"
                role="status"
              >
                Registration form submitted successfully.
                Account creation will be connected to the
                backend later.
              </p>
            )}
          </form>

          <p className="auth-footer-text">
            Already have an account?{" "}
            <Link to="/login">
              Login
            </Link>
          </p>
        </div>
      </div>
    </section>
  );
}

export default Register;