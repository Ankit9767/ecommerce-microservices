import React, {
  useEffect,
  useState
} from "react";

import "./styles/ReviewForm.css";

function StarIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="review-form-icon"
    >
      <path d="m12 4 2.1 4.3 4.7.7-3.4 3.3.8 4.7-4.2-2.2-4.2 2.2.8-4.7-3.4-3.3 4.7-.7L12 4Z" />
    </svg>
  );
}

function EditIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="review-form-icon"
    >
      <path d="m4 16.5-.8 3.3 3.3-.8L17.8 7.7a2.2 2.2 0 0 0-3.1-3.1L4 16.5Z" />
      <path d="m13.5 6.5 4 4" />
    </svg>
  );
}

function MessageIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="review-form-icon"
    >
      <path d="M5 5.5h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-8l-4.5 3v-3H5a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2Z" />
      <path d="M7 10h10" />
      <path d="M7 13.5h7" />
    </svg>
  );
}

function CheckIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="review-form-button-icon"
    >
      <path d="m5 12 4 4L19 6" />
    </svg>
  );
}

function CloseIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="review-form-button-icon"
    >
      <path d="m7 7 10 10" />
      <path d="m17 7-10 10" />
    </svg>
  );
}

function ReviewForm({
  review = null,
  onSubmit,
  onCancel,
  isSubmitting = false,
  error = ""
}) {
  const isEditing = Boolean(review);

  const [rating, setRating] = useState(
    review?.rating || 5
  );

  const [title, setTitle] = useState(
    review?.title || ""
  );

  const [comment, setComment] = useState(
    review?.comment || ""
  );

  useEffect(() => {
    setRating(review?.rating || 5);
    setTitle(review?.title || "");
    setComment(review?.comment || "");
  }, [review]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (isSubmitting) {
      return;
    }

    if (!rating || rating < 1 || rating > 5) {
      return;
    }

    if (!isEditing && !comment.trim()) {
      return;
    }

    await onSubmit({
      rating,
      title: title.trim(),
      comment: comment.trim()
    });
  };

  return (
    <form
      className="review-form"
      onSubmit={handleSubmit}
    >
      <div
        className="review-form-field review-form-field-rating"
        style={{
          "--review-field-delay": "0ms"
        }}
      >
        <label
          className="review-form-label"
          htmlFor={
            isEditing
              ? "review-rating-edit"
              : "review-rating"
          }
        >
          <span className="review-form-label-icon">
            <StarIcon />
          </span>

          <span>Rating</span>
        </label>

        <div
          className="review-rating-options"
          role="radiogroup"
          aria-label="Rating"
        >
          {Array.from({ length: 5 }).map(
            (_, index) => {
              const value = index + 1;

              return (
                <label
                  className={`review-rating-option ${
                    rating >= value
                      ? "review-rating-option-filled"
                      : ""
                  } ${
                    rating === value
                      ? "review-rating-option-selected"
                      : ""
                  }`}
                  key={value}
                >
                  <input
                    type="radio"
                    name="review-rating"
                    value={value}
                    checked={rating === value}
                    onChange={() => setRating(value)}
                  />

                  <span
                    className="review-rating-star"
                    aria-hidden="true"
                  >
                    <StarIcon />
                  </span>

                  <span className="sr-only">
                    {value}{" "}
                    {value === 1 ? "star" : "stars"}
                  </span>
                </label>
              );
            }
          )}
        </div>
      </div>

      <div
        className="review-form-field"
        style={{
          "--review-field-delay": "70ms"
        }}
      >
        <label
          className="review-form-label"
          htmlFor={
            isEditing
              ? "review-title-edit"
              : "review-title"
          }
        >
          <span className="review-form-label-icon">
            <EditIcon />
          </span>

          <span>Title</span>
        </label>

        <div className="review-form-control">
          <input
            id={
              isEditing
                ? "review-title-edit"
                : "review-title"
            }
            className="review-form-input"
            type="text"
            value={title}
            maxLength={150}
            onChange={(event) =>
              setTitle(event.target.value)
            }
            placeholder="Summarize your experience"
          />

          <span className="review-form-counter">
            {title.length}/150
          </span>
        </div>
      </div>

      <div
        className="review-form-field"
        style={{
          "--review-field-delay": "140ms"
        }}
      >
        <label
          className="review-form-label"
          htmlFor={
            isEditing
              ? "review-comment-edit"
              : "review-comment"
          }
        >
          <span className="review-form-label-icon">
            <MessageIcon />
          </span>

          <span>Review</span>
        </label>

        <div className="review-form-control">
          <textarea
            id={
              isEditing
                ? "review-comment-edit"
                : "review-comment"
            }
            className="review-form-textarea"
            value={comment}
            maxLength={5000}
            required={!isEditing}
            onChange={(event) =>
              setComment(event.target.value)
            }
            placeholder="Tell other customers about your experience"
            rows={6}
          />

          <span className="review-form-counter">
            {comment.length}/5000
          </span>
        </div>
      </div>

      {error && (
        <p
          className="review-form-error"
          role="alert"
        >
          <span className="review-form-error-icon">
            !
          </span>

          <span>{error}</span>
        </p>
      )}

      <div className="review-form-actions">
        <button
          type="submit"
          className="button review-form-submit"
          disabled={
            isSubmitting ||
            !rating ||
            rating < 1 ||
            rating > 5 ||
            (!isEditing &&
              !comment.trim())
          }
        >
          <span>
            {isSubmitting
              ? isEditing
                ? "Saving..."
                : "Submitting..."
              : isEditing
                ? "Save Review"
                : "Submit Review"}
          </span>
        </button>

        {onCancel && (
          <button
            type="button"
            className="review-form-cancel"
            onClick={onCancel}
            disabled={isSubmitting}
          >
            <span>Cancel</span>
          </button>
        )}
      </div>
    </form>
  );
}

export default ReviewForm;