import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";

import FormField from "../components/FormField";
import { useAuth } from "../context/AuthContext";

import "./styles/Login.css";

function UserIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="login-icon">
      <circle cx="12" cy="8" r="3.5" />
      <path d="M5 20c.8-3.5 3.1-5.5 7-5.5s6.2 2 7 5.5" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="login-icon">
      <rect x="5" y="10" width="14" height="10" rx="2" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
    </svg>
  );
}

function EyeIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="login-icon">
      <path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z" />
      <circle cx="12" cy="12" r="2.5" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="login-icon">
      <path d="M3 3l18 18" />
      <path d="M10.6 10.7a2 2 0 0 0 2.7 2.7" />
      <path d="M9.9 4.4A10.9 10.9 0 0 1 12 4c5 0 8.5 4 9.5 6a14.8 14.8 0 0 1-3.1 3.8" />
      <path d="M6.2 6.2C3.9 7.7 2.7 9.6 2.5 10c1 2 4.5 6 9.5 6 1 0 1.9-.2 2.7-.5" />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="login-icon">
      <path d="M5 12h13" />
      <path d="m13 6 6 6-6 6" />
    </svg>
  );
}

function SparkleIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="login-icon">
      <path d="m12 3 1.5 5.5L19 10l-5.5 1.5L12 17l-1.5-5.5L5 10l5.5-1.5L12 3Z" />
      <path d="m19 15 .7 2.3L22 18l-2.3.7L19 21l-.7-2.3L16 18l2.3-.7L19 15Z" />
    </svg>
  );
}

function Login() {
  const navigate = useNavigate();
  const location = useLocation();

  const { login } = useAuth();

  const [form, setForm] = useState({
    usernameOrEmail: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const redirectPath = location.state?.from?.pathname || "/";

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
      await login(form);

      navigate(redirectPath, {
        replace: true,
      });
    } catch (requestError) {
      setError(requestError.message || "Unable to log in. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="page login-page">
      <div className="login-decoration" aria-hidden="true">
        <div className="login-orbit login-orbit-one" />
        <div className="login-orbit login-orbit-two" />

        <div className="login-decoration-dot login-dot-one" />
        <div className="login-decoration-dot login-dot-two" />
        <div className="login-decoration-dot login-dot-three" />
      </div>

      <div className="container">
        <div className="login-layout">
          {/* LEFT SIDE */}
          <div className="login-intro">
            <div className="login-eyebrow">
              <span className="login-eyebrow-icon">
                <SparkleIcon />
              </span>

              <span>Welcome back</span>
            </div>

            <h1 className="login-title">
              Your shopping
              <span> journey continues.</span>
            </h1>

            <p className="login-description">
              Sign in to EcommerceHub to explore products, manage your orders,
              and continue where you left off.
            </p>

            <div className="login-note">
              <span className="login-note-dot" />

              <span>Your account is waiting for you</span>
            </div>

            <div className="login-floating-card login-floating-card-top">
              <UserIcon />

              <div>
                <strong>Welcome back</strong>
                <span>Good to see you again</span>
              </div>
            </div>

            <div className="login-floating-card login-floating-card-bottom">
              <LockIcon />

              <div>
                <strong>Ready to explore?</strong>
                <span>Your next find is waiting</span>
              </div>
            </div>
          </div>

          {/* LOGIN CARD */}
          <div className="login-card">
            <div className="login-card-header">
              <p className="login-card-eyebrow">Account access</p>

              <h2>Sign in</h2>

              <p>Enter your details to access your EcommerceHub account.</p>
            </div>

            {error && (
              <div className="login-error" role="alert">
                <span className="login-error-icon">!</span>

                <span>{error}</span>
              </div>
            )}

            <form className="login-form" onSubmit={handleSubmit}>
              <div className="login-form-field">
                <div className="login-field-icon">
                  <UserIcon />
                </div>

                <FormField
                  id="usernameOrEmail"
                  name="usernameOrEmail"
                  label="Username or Email"
                  value={form.usernameOrEmail}
                  onChange={handleChange}
                  required
                  autoComplete="username"
                />
              </div>

              <div className="login-form-field login-password-field">
                <div className="login-field-icon">
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
                  autoComplete="current-password"
                />

                <button
                  type="button"
                  className="login-password-toggle"
                  onClick={() => setShowPassword((current) => !current)}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>

              <button
                type="submit"
                className="login-submit-button"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    <span className="login-spinner" />
                    <span>Signing in...</span>
                  </>
                ) : (
                  <>
                    <span>Sign in</span>
                    <ArrowIcon />
                  </>
                )}
              </button>
            </form>

            <div className="login-divider">
              <span>New to EcommerceHub?</span>
            </div>

            <Link to="/register" className="login-register-button">
              <span>Create an account</span>
              <ArrowIcon />
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}

export default Login;
