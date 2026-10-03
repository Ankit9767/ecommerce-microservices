import React, { useEffect, useRef } from "react";

import "./styles/ConfirmModal.css";

function AlertIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="confirm-modal-icon">
      <path d="M12 3 21 19H3L12 3Z" />
      <path d="M12 9v4" />
      <circle cx="12" cy="16.5" r="0.8" fill="currentColor" stroke="none" />
    </svg>
  );
}

function ConfirmModal({
  isOpen,
  title = "Are you sure?",
  message = "Please confirm this action.",
  confirmText = "Confirm",
  cancelText = "Cancel",
  onConfirm,
  onCancel,
  isLoading = false,
  loadingText = "Please wait...",
}) {
  const cancelButtonRef = useRef(null);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    cancelButtonRef.current?.focus();

    const handleKeyDown = (event) => {
      if (event.key === "Escape" && !isLoading) {
        onCancel();
      }
    };

    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, isLoading, onCancel]);

  if (!isOpen) {
    return null;
  }

  const handleBackdropClick = (event) => {
    if (event.target === event.currentTarget && !isLoading) {
      onCancel();
    }
  };

  return (
    <div
      className="confirm-modal-backdrop"
      role="presentation"
      onMouseDown={handleBackdropClick}
    >
      <div
        className="confirm-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-modal-title"
        aria-describedby="confirm-modal-message"
      >
        <div className="confirm-modal-header">
          <div className="confirm-modal-icon-wrapper">
            <AlertIcon />
          </div>

          <div className="confirm-modal-content">
            <p className="confirm-modal-eyebrow">Confirmation</p>

            <h2 id="confirm-modal-title" className="confirm-modal-title">
              {title}
            </h2>

            <p id="confirm-modal-message" className="confirm-modal-message">
              {message}
            </p>
          </div>
        </div>

        <div className="confirm-modal-actions">
          <button
            ref={cancelButtonRef}
            type="button"
            className="confirm-modal-button confirm-modal-cancel"
            onClick={onCancel}
            disabled={isLoading}
          >
            {cancelText}
          </button>

          <button
            type="button"
            className="confirm-modal-button confirm-modal-confirm"
            onClick={onConfirm}
            disabled={isLoading}
            aria-busy={isLoading}
          >
            {isLoading ? (
              <>
                <span className="confirm-modal-spinner" aria-hidden="true" />
                <span>{loadingText}</span>
              </>
            ) : (
              <span>{confirmText}</span>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmModal;
