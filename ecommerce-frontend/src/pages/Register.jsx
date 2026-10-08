import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import FormField from "../components/FormField";
import { useAuth } from "../context/AuthContext";

import "./styles/Register.css";

function UserIcon() {
  return (
    <svg className="register-icon" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="8" r="3.5" />
      <path d="M5 20c.8-3.5 3.1-5.5 7-5.5s6.2 2 7 5.5" />
    </svg>
  );
}

function MailIcon() {
  return (
    <svg className="register-icon" viewBox="0 0 24 24" aria-hidden="true">
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <path d="m4 7 8 6 8-6" />
    </svg>
  );
}

function PhoneIcon() {
  return (
    <svg className="register-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M7 3h3l1.5 4-2 1.5c.9 2 2.5 3.6 4.5 4.5L15.5 11 19 12.5v3c0 1.1-.9 2-2 2C10.4 17.5 6.5 13.6 6.5 7 6.5 4.8 6.7 3 7 3Z" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg className="register-icon" viewBox="0 0 24 24" aria-hidden="true">
      <rect x="5" y="10" width="14" height="10" rx="2" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
    </svg>
  );
}

function EyeIcon() {
  return (
    <svg className="register-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z" />
      <circle cx="12" cy="12" r="2.5" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg className="register-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M3 3l18 18" />
      <path d="M10.6 10.7a2 2 0 0 0 2.7 2.7" />
      <path d="M9.9 4.4A10.9 10.9 0 0 1 12 4c5 0 8.5 4 9.5 6a14.8 14.8 0 0 1-3.1 3.8" />
      <path d="M6.2 6.2C3.9 7.7 2.7 9.6 2.5 10c1 2 4.5 6 9.5 6 1 0 1.9-.2 2.7-.5" />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg className="register-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M5 12h13" />
      <path d="m13 6 6 6-6 6" />
    </svg>
  );
}

function SparkleIcon() {
  return (
    <svg className="register-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="m12 3 1.5 5.5L19 10l-5.5 1.5L12 17l-1.5-5.5L5 10l5.5-1.5L12 3Z" />
      <path d="m19 15 .7 2.3L22 18l-2.3.7L19 21l-.7-2.3L16 18l2.3-.7L19 15Z" />
    </svg>
  );
}

function Register() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [form, setForm] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    phone: "",
  });

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }));

    if (error) {
      setError("");
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError("");
    setIsSubmitting(true);

    try {
      await register(form);

      navigate("/", {
        replace: true,
      });
    } catch (requestError) {
      setError(requestError.message || "Unable to create your account.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="page register-page">
      <div className="register-decoration" aria-hidden="true">
        <div className="register-orbit register-orbit-one" />
        <div className="register-orbit register-orbit-two" />

        <div className="register-decoration-dot register-dot-one" />
        <div className="register-decoration-dot register-dot-two" />
        <div className="register-decoration-dot register-dot-three" />
      </div>

      <div className="container">
        <div className="register-layout">
          {/* LEFT SIDE */}
          <div className="register-intro">
            <div className="register-eyebrow">
              <span className="register-eyebrow-icon">
                <SparkleIcon />
              </span>

              <span>Join EcommerceHub</span>
            </div>

            <h1 className="register-title">
              Start your shopping
              <span> journey today.</span>
            </h1>

            <p className="register-description">
              Create your EcommerceHub account to discover products, manage your
              orders, and enjoy a simple shopping experience.
            </p>

            <div className="register-note">
              <span className="register-note-dot" />

              <span>Everything you need, all in one place</span>
            </div>

            <div className="register-floating-card register-floating-card-top">
              <UserIcon />

              <div>
                <strong>Make it yours</strong>
                <span>A shopping experience made for you</span>
              </div>
            </div>

            <div className="register-floating-card register-floating-card-bottom">
              <LockIcon />

              <div>
                <strong>Discover more</strong>
                <span>Find products you'll love</span>
              </div>
            </div>
          </div>

          {/* REGISTER CARD */}
          <div className="register-card">
            <div className="register-card-header">
              <p className="register-card-eyebrow">Create your account</p>

              <h2>Get started</h2>

              <p>Enter your details to create your EcommerceHub account.</p>
            </div>

            {error && (
              <div className="register-error" role="alert">
                <span className="register-error-icon">!</span>

                <span>{error}</span>
              </div>
            )}

            <form className="register-form" onSubmit={handleSubmit}>
              <div className="register-form-field">
                <div className="register-field-icon">
                  <UserIcon />
                </div>

                <FormField
                  id="username"
                  name="username"
                  label="Username"
                  value={form.username}
                  onChange={handleChange}
                  required
                  autoComplete="username"
                />
              </div>

              <div className="register-name-fields">
                <div className="register-form-field">
                  <div className="register-field-icon">
                    <UserIcon />
                  </div>

                  <FormField
                    id="firstName"
                    name="firstName"
                    label="First Name"
                    value={form.firstName}
                    onChange={handleChange}
                    required
                    autoComplete="given-name"
                  />
                </div>

                <div className="register-form-field">
                  <div className="register-field-icon">
                    <UserIcon />
                  </div>

                  <FormField
                    id="lastName"
                    name="lastName"
                    label="Last Name"
                    value={form.lastName}
                    onChange={handleChange}
                    required
                    autoComplete="family-name"
                  />
                </div>
              </div>

              <div className="register-form-field">
                <div className="register-field-icon">
                  <MailIcon />
                </div>

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
              </div>

              <div className="register-form-field">
                <div className="register-field-icon">
                  <PhoneIcon />
                </div>

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
              </div>

              <div className="register-form-field register-password-field">
                <div className="register-field-icon">
                  <LockIcon />
                </div>

                <FormField
                  id="password"
                  name="password"
                  label="Password"
                  type={showPassword ? "text" : "password"}
                  value={form.password}
                  onChange={handleChange}
                  required
                  autoComplete="new-password"
                />

                <button
                  type="button"
                  className="register-password-toggle"
                  onClick={() => setShowPassword((current) => !current)}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>

              <button
                type="submit"
                className="register-submit-button"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    <span className="register-spinner" />
                    <span>Creating account...</span>
                  </>
                ) : (
                  <>
                    <span>Create account</span>
                    <ArrowIcon />
                  </>
                )}
              </button>
            </form>

            <div className="register-divider">
              <span>Already have an account?</span>
            </div>

            <Link to="/login" className="register-login-button">
              <span>Sign in</span>
              <ArrowIcon />
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}

export default Register;
