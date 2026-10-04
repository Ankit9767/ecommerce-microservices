import React from "react";
import "./styles/ReviewCard.css";

function formatReviewDate(dateValue) {
  if (!dateValue) {
    return "";
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return dateValue;
  }

  return date.toLocaleDateString();
}

function ReviewStars({ rating }) {
  const normalizedRating = Math.min(5, Math.max(0, Number(rating) || 0));

  return (
    <div
      className="review-stars"
      aria-label={`${normalizedRating} out of 5 stars`}
    >
      {Array.from({ length: 5 }).map((_, index) => (
        <span
          key={index}
          className={
            index < normalizedRating
              ? "review-star review-star-filled"
              : "review-star"
          }
          aria-hidden="true"
        >
          ★
        </span>
      ))}
    </div>
  );
}

function ReviewCard({
  review,
  isOwnReview = false,
  onEdit,
  onDelete,
  isDeleting = false,
}) {
  if (!review) {
    return null;
  }

  return (
    <article className="review-card">
      <div className="review-card-header">
        <div>
          <ReviewStars rating={review.rating} />

          {review.title && (
            <h3 className="review-card-title">{review.title}</h3>
          )}
        </div>

        <time
          className="review-card-date"
          dateTime={review.createdAt || undefined}
        >
          {formatReviewDate(review.createdAt)}
        </time>
      </div>

      <p className="review-card-comment">{review.comment}</p>

      {isOwnReview && (
        <div className="review-card-actions">
          {onEdit && (
            <button
              type="button"
              className="review-card-action"
              onClick={() => onEdit(review)}
              disabled={isDeleting}
            >
              Edit
            </button>
          )}

          {onDelete && (
            <button
              type="button"
              className="review-card-action review-card-action-danger"
              onClick={() => onDelete(review)}
              disabled={isDeleting}
            >
              {isDeleting ? "Deleting..." : "Delete"}
            </button>
          )}
        </div>
      )}
    </article>
  );
}

export default ReviewCard;