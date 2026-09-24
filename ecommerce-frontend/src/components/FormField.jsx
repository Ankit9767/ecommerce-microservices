import React from "react";

import "./styles/FormField.css";

function FormField({
  id,
  name,
  label,
  type = "text",
  value,
  onChange,
  placeholder,
  required = false,
  error,
  autoComplete
}) {
  return (
    <div className="form-field">
      <label
        className="form-field-label"
        htmlFor={id}
      >
        {label}
      </label>

      <input
        id={id}
        name={name}
        className={`form-field-input ${
          error ? "form-field-input-error" : ""
        }`}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        autoComplete={autoComplete}
        aria-invalid={Boolean(error)}
        aria-describedby={
          error ? `${id}-error` : undefined
        }
      />

      {error && (
        <p
          id={`${id}-error`}
          className="form-field-error"
          role="alert"
        >
          {error}
        </p>
      )}
    </div>
  );
}

export default FormField;