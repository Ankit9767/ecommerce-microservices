import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import FormField from "../components/FormField";
import { useAuth } from "../context/AuthContext";

import "./styles/Register.css";

function Register() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [form, setForm] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    phone: ""
  });

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] =
    useState(false);

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
      await register(form);

      navigate("/login", {
        replace: true,
        state: {
          registered: true
        }
      });
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to create your account."
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="page register-page">
      <div className="container">
        <div className="register-card">
          <h1 className="page-title">
            Create Account
          </h1>

          <p className="page-description">
            Create your EcommerceHub account.
          </p>

          {error && (
            <p
              className="register-error"
              role="alert"
            >
              {error}
            </p>
          )}

          <form
            className="register-form"
            onSubmit={handleSubmit}
          >
            <FormField
              id="username"
              name="username"
              label="Username"
              value={form.username}
              onChange={handleChange}
              required
              autoComplete="username"
            />

            <FormField
              id="firstName"
              name="firstName"
              label="First Name"
              value={form.firstName}
              onChange={handleChange}
              required
              autoComplete="given-name"
            />

            <FormField
              id="lastName"
              name="lastName"
              label="Last Name"
              value={form.lastName}
              onChange={handleChange}
              required
              autoComplete="family-name"
            />

            <FormField
              id="email"
              name="email"
              label="Email"
              type="email"
              value={form.email}
              onChange={handleChange}
              required
              autoComplete="email"
            />

            <FormField
              id="phone"
              name="phone"
              label="Phone"
              type="tel"
              value={form.phone}
              onChange={handleChange}
              required
              autoComplete="tel"
            />

            <FormField
              id="password"
              name="password"
              label="Password"
              type="password"
              value={form.password}
              onChange={handleChange}
              required
              autoComplete="new-password"
            />

            <button
              type="submit"
              className="button"
              disabled={isSubmitting}
            >
              {isSubmitting
                ? "Creating Account..."
                : "Create Account"}
            </button>
          </form>

          <p className="register-login-link">
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