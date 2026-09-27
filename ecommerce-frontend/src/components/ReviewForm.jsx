import React, {
  useEffect,
  useState
} from "react";

import "./styles/ReviewForm.css";

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
      <div className="review-form-field">
        <label
          className="review-form-label"
          htmlFor={
            isEditing
              ? "review-rating-edit"
              : "review-rating"
          }
        >
          Rating
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
                    checked={
                      rating === value
                    }
                    onChange={() =>
                      setRating(value)
                    }
                  />

                  <span aria-hidden="true">
                    ★
                  </span>

                  <span className="sr-only">
                    {value}{" "}
                    {value === 1
                      ? "star"
                      : "stars"}
                  </span>
                </label>
              );
            }
          )}
        </div>
      </div>

      <div className="review-form-field">
        <label
          className="review-form-label"
          htmlFor={
            isEditing
              ? "review-title-edit"
              : "review-title"
          }
        >
          Title
        </label>

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

      <div className="review-form-field">
        <label
          className="review-form-label"
          htmlFor={
            isEditing
              ? "review-comment-edit"
              : "review-comment"
          }
        >
          Review
        </label>

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

      {error && (
        <p
          className="review-form-error"
          role="alert"
        >
          {error}
        </p>
      )}

      <div className="review-form-actions">
        <button
          type="submit"
          className="button"
          disabled={
            isSubmitting ||
            !rating ||
            rating < 1 ||
            rating > 5 ||
            (!isEditing &&
              !comment.trim())
          }
        >
          {isSubmitting
            ? isEditing
              ? "Saving..."
              : "Submitting..."
            : isEditing
              ? "Save Review"
              : "Submit Review"}
        </button>

        {onCancel && (
          <button
            type="button"
            className="review-form-cancel"
            onClick={onCancel}
            disabled={isSubmitting}
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}

export default ReviewForm;