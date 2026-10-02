
import React, {
  useCallback,
  useEffect,
  useState
} from "react";

import {
  Link,
  useParams
} from "react-router-dom";

import { useAuth } from "../context/AuthContext";

import ReviewCard from "../components/ReviewCard";
import ReviewForm from "../components/ReviewForm";

import {
  createReview,
  deleteReview,
  getMyReviews,
  updateReview
} from "../services/reviewService";

import {
  cancelOrder,
  getOrder
} from "../services/orderService";

import {
  getMyPayments,
  getPayment,
  initializeCheckout
} from "../services/paymentService";

import {
  openRazorpayCheckout,
  toRazorpayAmount
} from "../services/razorpayService";

import {
  trackShipmentByOrderId
} from "../services/shipmentService";

import "./styles/OrderDetails.css";

const PAYMENT_POLL_INTERVAL = 1500;
const PAYMENT_POLL_ATTEMPTS = 20;

const SHIPMENT_POLL_INTERVAL = 10000;

const SHIPMENT_STEPS = [
  {
    status: "CREATED",
    label: "Order Processing",
    description: "Your shipment has been created."
  },
  {
    status: "IN_TRANSIT",
    label: "In Transit",
    description: "Your shipment is on its way."
  },
  {
    status: "OUT_FOR_DELIVERY",
    label: "Out for Delivery",
    description: "Your shipment is out for delivery."
  },
  {
    status: "DELIVERED",
    label: "Delivered",
    description: "Your shipment has been delivered."
  }
];

function OrderIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <path d="M4 6.5 12 3l8 3.5v11L12 21l-8-3.5v-11Z" />
      <path d="M4 6.5 12 10l8-3.5" />
      <path d="M12 10v11" />
      <path d="M8 8.25v4.5" />
    </svg>
  );
}

function ProductsIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="nav-icon"
    >
      <rect x="4" y="4" width="16" height="16" rx="2" />
      <path d="M4 9h16" />
      <path d="M9 9v11" />
    </svg>
  );
}

function PackageIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z" />
      <path d="m4 7.5 8 4.5 8-4.5" />
      <path d="M12 12v9" />
    </svg>
  );
}

function ArrowIcon({ direction = "right" }) {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      {direction === "left" ? (
        <>
          <path d="M19 12H5" />
          <path d="m11 18-6-6 6-6" />
        </>
      ) : (
        <>
          <path d="M5 12h14" />
          <path d="m13 6 6 6-6 6" />
        </>
      )}
    </svg>
  );
}

function PaymentIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <rect
        x="3.5"
        y="5"
        width="17"
        height="14"
        rx="2"
      />
      <path d="M3.5 9h17" />
      <path d="M7 14h4" />
    </svg>
  );
}

function TruckIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <path d="M3 6h11v11H3z" />
      <path d="M14 10h4l3 3v4h-7z" />
      <circle cx="7" cy="18" r="1.7" />
      <circle cx="18" cy="18" r="1.7" />
    </svg>
  );
}

function ReviewIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <path d="m12 4 2.1 4.3 4.7.7-3.4 3.3.8 4.7-4.2-2.2-4.2 2.2.8-4.7-3.4-3.3 4.7-.7L12 4Z" />
    </svg>
  );
}

function LocationIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <path d="M20 10.5c0 5.2-8 10.5-8 10.5S4 15.7 4 10.5a8 8 0 1 1 16 0Z" />
      <circle cx="12" cy="10.5" r="2.5" />
    </svg>
  );
}

function CheckIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <path d="m5 12 4 4L19 6" />
    </svg>
  );
}

function ClockIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7v5l3 2" />
    </svg>
  );
}

function AlertIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className="order-details-icon"
    >
      <path d="M12 4 21 20H3L12 4Z" />
      <path d="M12 9v5" />
      <path d="M12 17h.01" />
    </svg>
  );
}

function formatShipmentDate(dateValue) {
  if (!dateValue) {
    return null;
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return dateValue;
  }

  return date.toLocaleString();
}

function getShipmentStepIndex(status) {
  const normalizedStatus =
    String(status || "").toUpperCase();

  return SHIPMENT_STEPS.findIndex(
    (step) => step.status === normalizedStatus
  );
}

function getShipmentStatusLabel(status) {
  const normalizedStatus =
    String(status || "").toUpperCase();

  const matchingStep = SHIPMENT_STEPS.find(
    (step) => step.status === normalizedStatus
  );

  if (matchingStep) {
    return matchingStep.label;
  }

  return normalizedStatus
    .replace(/_/g, " ")
    .toLowerCase()
    .replace(/\b\w/g, (character) =>
      character.toUpperCase()
    );
}

function OrderDetails() {
  const { id } = useParams();

  const {
    isAuthenticated,
    isLoading: isAuthLoading
  } = useAuth();

  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(null);
  const [shipment, setShipment] = useState(null);

  const [isLoading, setIsLoading] = useState(true);

  const [isPaymentLoading, setIsPaymentLoading] =
    useState(false);

  const [isProcessingPayment, setIsProcessingPayment] =
    useState(false);

  const [isCancelling, setIsCancelling] =
    useState(false);

  const [isShipmentLoading, setIsShipmentLoading] =
    useState(false);

  const [error, setError] = useState("");

  const [paymentError, setPaymentError] =
    useState("");

  const [paymentMessage, setPaymentMessage] =
    useState("");

  const [cancelError, setCancelError] =
    useState("");

  const [shipmentError, setShipmentError] =
    useState("");

  const [myReviews, setMyReviews] =
    useState([]);

  const [isReviewsLoading, setIsReviewsLoading] =
    useState(false);

  const [reviewError, setReviewError] =
    useState("");

  const [reviewFormProductId, setReviewFormProductId] =
    useState(null);

  const [editingReview, setEditingReview] =
    useState(null);

  const [isReviewSubmitting, setIsReviewSubmitting] =
    useState(false);

  const [deletingReviewId, setDeletingReviewId] =
    useState(null);

  useEffect(() => {
    let isMounted = true;
    let timeoutId = null;

    async function loadOrder() {
      if (!id) {
        setError("Order ID is missing.");
        setIsLoading(false);
        return;
      }

      if (!isAuthenticated) {
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError("");

      timeoutId = window.setTimeout(() => {
        if (isMounted) {
          setIsLoading(false);

          setError(
            "The order request timed out. Please try again."
          );
        }
      }, 15000);

      try {
        const response = await getOrder(id);

        if (!isMounted) {
          return;
        }

        setOrder(response);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        if (requestError?.status === 404) {
          setError("Order not found.");
        } else {
          setError(
            requestError?.message ||
              "Unable to load this order."
          );
        }
      } finally {
        if (timeoutId) {
          window.clearTimeout(timeoutId);
        }

        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    if (!isAuthLoading) {
      loadOrder();
    }

    return () => {
      isMounted = false;

      if (timeoutId) {
        window.clearTimeout(timeoutId);
      }
    };
  }, [
    id,
    isAuthenticated,
    isAuthLoading
  ]);

  useEffect(() => {
    let isMounted = true;

    async function loadMyReviews() {
      if (!isAuthenticated) {
        if (isMounted) {
          setMyReviews([]);
          setIsReviewsLoading(false);
        }

        return;
      }

      setIsReviewsLoading(true);
      setReviewError("");

      try {
        const response = await getMyReviews({
          page: 0,
          size: 100,
          sort: "createdAt,desc"
        });

        if (!isMounted) {
          return;
        }

        setMyReviews(
          response?.content || []
        );
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setReviewError(
          requestError?.message ||
            "Unable to load your reviews."
        );

        setMyReviews([]);
      } finally {
        if (isMounted) {
          setIsReviewsLoading(false);
        }
      }
    }

    if (!isAuthLoading) {
      loadMyReviews();
    }

    return () => {
      isMounted = false;
    };
  }, [
    isAuthenticated,
    isAuthLoading
  ]);

  const getReviewForProduct = useCallback(
    (productId) => {
      if (!order?.id || !productId) {
        return null;
      }

      return (
        myReviews.find(
          (review) =>
            Number(review.orderId) ===
              Number(order.id) &&
            Number(review.productId) ===
              Number(productId)
        ) || null
      );
    },
    [
      myReviews,
      order
    ]
  );

  const handleReviewSubmit = async ({
    rating,
    title,
    comment
  }) => {
    if (
      !order?.id ||
      !reviewFormProductId ||
      isReviewSubmitting
    ) {
      return;
    }

    setIsReviewSubmitting(true);
    setReviewError("");

    try {
      const existingReview =
        getReviewForProduct(
          reviewFormProductId
        );

      let response;

      if (existingReview) {
        response = await updateReview(
          existingReview.id,
          {
            rating,
            title,
            comment
          }
        );

        setMyReviews((current) =>
          current.map((review) =>
            review.id === response.id
              ? response
              : review
          )
        );
      } else {
        response = await createReview({
          productId:
            reviewFormProductId,
          orderId: order.id,
          rating,
          title,
          comment
        });

        setMyReviews((current) => [
          response,
          ...current
        ]);
      }

      setReviewFormProductId(null);
      setEditingReview(null);
    } catch (requestError) {
      setReviewError(
        requestError?.message ||
          "Unable to save your review."
      );
    } finally {
      setIsReviewSubmitting(false);
    }
  };

  const handleEditReview = (
    review
  ) => {
    setReviewError("");
    setEditingReview(review);
    setReviewFormProductId(
      review.productId
    );
  };

  const handleDeleteReview = async (
    review
  ) => {
    if (
      !review?.id ||
      deletingReviewId
    ) {
      return;
    }

    const confirmed = window.confirm(
      "Are you sure you want to delete this review?"
    );

    if (!confirmed) {
      return;
    }

    setDeletingReviewId(review.id);
    setReviewError("");

    try {
      await deleteReview(review.id);

      setMyReviews((current) =>
        current.filter(
          (item) =>
            item.id !== review.id
        )
      );

      if (
        editingReview?.id ===
        review.id
      ) {
        setEditingReview(null);
        setReviewFormProductId(null);
      }
    } catch (requestError) {
      setReviewError(
        requestError?.message ||
          "Unable to delete your review."
      );
    } finally {
      setDeletingReviewId(null);
    }
  };

  const loadShipment = useCallback(
    async (showLoading = true) => {
      if (
        !isAuthenticated ||
        !id
      ) {
        return null;
      }

      if (showLoading) {
        setIsShipmentLoading(true);
      }

      setShipmentError("");

      try {
        const response =
          await trackShipmentByOrderId(id);

        setShipment(response);

        return response;
      } catch (requestError) {
        if (
          requestError?.status === 404
        ) {
          setShipment(null);
          return null;
        }

        setShipmentError(
          requestError?.message ||
            "Unable to load shipment tracking."
        );

        return null;
      } finally {
        if (showLoading) {
          setIsShipmentLoading(false);
        }
      }
    },
    [
      id,
      isAuthenticated
    ]
  );

  useEffect(() => {
    if (
      !isAuthenticated ||
      !order ||
      !id ||
      order.status ===
        "PENDING_PAYMENT"
    ) {
      return undefined;
    }

    let isMounted = true;
    let intervalId = null;

    async function loadInitialShipment() {
      const response =
        await loadShipment(true);

      if (!isMounted) {
        return;
      }

      if (
        response?.status ===
          "DELIVERED" ||
        response?.status ===
          "CANCELLED" ||
        response?.status ===
          "FAILED"
      ) {
        return;
      }

      intervalId =
        window.setInterval(
          async () => {
            if (!isMounted) {
              return;
            }

            const latestShipment =
              await loadShipment(false);

            if (
              latestShipment?.status ===
                "DELIVERED" ||
              latestShipment?.status ===
                "CANCELLED" ||
              latestShipment?.status ===
                "FAILED"
            ) {
              if (intervalId) {
                window.clearInterval(
                  intervalId
                );

                intervalId = null;
              }
            }
          },
          SHIPMENT_POLL_INTERVAL
        );
    }

    loadInitialShipment();

    return () => {
      isMounted = false;

      if (intervalId) {
        window.clearInterval(
          intervalId
        );
      }
    };
  }, [
    isAuthenticated,
    order,
    id,
    loadShipment
  ]);

  const findPaymentForOrder =
    useCallback(
      async () => {
        const response =
          await getMyPayments({
            page: 0,
            size: 100
          });

        const payments =
          response?.content || [];

        const matchingPayment =
          payments.find(
            (item) =>
              Number(item.orderId) ===
              Number(id)
          );

        if (matchingPayment) {
          setPayment(
            matchingPayment
          );
        }

        return (
          matchingPayment || null
        );
      },
      [id]
    );

  useEffect(() => {
    let isMounted = true;

    async function discoverPayment() {
      if (
        !order ||
        order.status !==
          "PENDING_PAYMENT"
      ) {
        return;
      }

      setIsPaymentLoading(true);
      setPaymentError("");

      for (
        let attempt = 0;
        attempt <
        PAYMENT_POLL_ATTEMPTS;
        attempt += 1
      ) {
        try {
          const foundPayment =
            await findPaymentForOrder();

          if (foundPayment) {
            if (isMounted) {
              setPayment(
                foundPayment
              );
            }

            return;
          }
        } catch (requestError) {
          if (isMounted) {
            setPaymentError(
              requestError?.message ||
                "Unable to find the payment for this order."
            );
          }

          return;
        }

        await new Promise(
          (resolve) => {
            setTimeout(
              resolve,
              PAYMENT_POLL_INTERVAL
            );
          }
        );
      }

      if (isMounted) {
        setPaymentError(
          "Your payment is still being prepared. Please refresh the page and try again."
        );
      }
    }

    if (
      isAuthenticated &&
      order?.status ===
        "PENDING_PAYMENT"
    ) {
      discoverPayment().finally(
        () => {
          if (isMounted) {
            setIsPaymentLoading(
              false
            );
          }
        }
      );
    }

    return () => {
      isMounted = false;
    };
  }, [
    order,
    isAuthenticated,
    findPaymentForOrder
  ]);

  const waitForPaymentResult =
    useCallback(
      async (paymentId) => {
        setPaymentMessage(
          "Payment submitted. Waiting for confirmation..."
        );

        setPaymentError("");

        for (
          let attempt = 0;
          attempt <
          PAYMENT_POLL_ATTEMPTS;
          attempt += 1
        ) {
          try {
            const latestPayment =
              await getPayment(
                paymentId
              );

            setPayment(
              latestPayment
            );

            if (
              latestPayment.status ===
              "SUCCESS"
            ) {
              setPaymentMessage(
                "Payment completed successfully."
              );

              try {
                const latestOrder =
                  await getOrder(id);

                setOrder(
                  latestOrder
                );
              } catch {
                // Payment is already confirmed.
              }

              return latestPayment;
            }

            if (
              latestPayment.status ===
                "FAILED" ||
              latestPayment.status ===
                "CANCELLED"
            ) {
              setPaymentError(
                latestPayment.failureReason ||
                  "Payment was not completed."
              );

              return latestPayment;
            }
          } catch (requestError) {
            setPaymentError(
              requestError?.message ||
                "Unable to check payment status."
            );

            return null;
          }

          await new Promise(
            (resolve) => {
              setTimeout(
                resolve,
                PAYMENT_POLL_INTERVAL
              );
            }
          );
        }

        setPaymentMessage(
          "Payment is still being processed. The order will update after the backend receives the payment confirmation."
        );

        return null;
      },
      [id]
    );

  const handleCompletePayment =
    useCallback(
      async () => {
        if (
          !payment?.id ||
          payment.status !==
            "PENDING" ||
          isProcessingPayment
        ) {
          return;
        }

        setIsProcessingPayment(
          true
        );

        setPaymentError("");
        setPaymentMessage("");

        try {
          const checkout =
            await initializeCheckout(
              payment.id
            );

          if (
            !checkout?.providerKeyId ||
            !checkout?.providerOrderId
          ) {
            throw new Error(
              "Payment checkout information is incomplete."
            );
          }

          await openRazorpayCheckout({
            key:
              checkout.providerKeyId,

            amount:
              toRazorpayAmount(
                checkout.amount
              ),

            currency:
              checkout.currency,

            orderId:
              checkout.providerOrderId,

            name:
              "EcommerceHub",

            description:
              `Order #${id}`,

            onSuccess:
              async () => {
                await waitForPaymentResult(
                  payment.id
                );

                setIsProcessingPayment(
                  false
                );
              },

            onFailure:
              (response) => {
                setPaymentError(
                  response?.error
                    ?.description ||
                    "Razorpay reported that the payment failed."
                );

                setIsProcessingPayment(
                  false
                );
              },

            onDismiss: () => {
              setPaymentMessage(
                "Payment window was closed. Your order is still awaiting payment."
              );

              setIsProcessingPayment(
                false
              );
            }
          });
        } catch (requestError) {
          setPaymentError(
            requestError?.message ||
              "Unable to start the payment."
          );

          setIsProcessingPayment(
            false
          );
        }
      },
      [
        id,
        payment,
        isProcessingPayment,
        waitForPaymentResult
      ]
    );

  const handleCancel =
    async () => {
      if (
        !order?.id ||
        isCancelling
      ) {
        return;
      }

      const confirmed =
        window.confirm(
          "Are you sure you want to cancel this order?"
        );

      if (!confirmed) {
        return;
      }

      setIsCancelling(true);
      setCancelError("");

      try {
        const response =
          await cancelOrder(
            order.id
          );

        setOrder(response);
      } catch (requestError) {
        setCancelError(
          requestError?.message ||
            "Unable to cancel this order."
        );
      } finally {
        setIsCancelling(false);
      }
    };

  if (
    isAuthLoading ||
    isLoading
  ) {
    return (
      <main className="page order-details-page">
        <div className="container">
          <div className="order-details-loading">
            <div className="order-details-loading-icon">
              <OrderIcon />
            </div>

            <p>Loading order...</p>

            <div className="order-details-loading-bar">
              <span />
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return (
      <main className="page order-details-page">
        <div className="container">
          <div className="order-details-state">
            <div className="order-details-state-icon">
              <OrderIcon />
            </div>

            <p className="order-details-state-eyebrow">
              EcommerceHub
            </p>

            <h1 className="page-title">
              Order Details
            </h1>

            <p>
              Please sign in to view your order.
            </p>

            <Link
              className="button order-details-primary-button"
              to="/login"
            >
              <span>Sign In</span>
              <ArrowIcon />
            </Link>
          </div>
        </div>
      </main>
    );
  }

  if (error) {
    return (
      <main className="page order-details-page">
        <div className="container">
          <div className="order-details-state order-details-error-state">
            <div className="order-details-state-icon">
              <AlertIcon />
            </div>

            <p className="order-details-state-eyebrow">
              Unable to load order
            </p>

            <p className="order-details-error">
              {error}
            </p>

            <Link
              className="button order-details-primary-button"
              to="/orders"
            >
              <ArrowIcon direction="left" />
              <span>Back to Orders</span>
            </Link>
          </div>
        </div>
      </main>
    );
  }

  if (!order) {
    return null;
  }

  const isPaymentPending =
    order.status ===
    "PENDING_PAYMENT";

  const canCancel =
    ![
      "CANCELLED",
      "SHIPPED",
      "DELIVERED"
    ].includes(
      order.status
    );

  const shipmentStatus =
    String(
      shipment?.status ||
        "CREATED"
    ).toUpperCase();

  const shipmentStepIndex =
    getShipmentStepIndex(
      shipmentStatus
    );

  const shipmentIsTerminal =
    shipmentStatus ===
      "DELIVERED" ||
    shipmentStatus ===
      "CANCELLED" ||
    shipmentStatus ===
      "FAILED";

  return (
    <main className="page order-details-page">
      <div className="container">

        <header className="order-details-header">
          <div className="order-details-header-main">
            <Link
              className="order-back-link"
              to="/orders"
            >
              <ArrowIcon direction="left" />
              <span>Back to Orders</span>
            </Link>

            <div className="order-details-title-row">
              <div className="order-details-title-icon">
                <OrderIcon />
              </div>

              <div>
                <p className="order-details-eyebrow">
                  Order Details
                </p>

                <h1 className="page-title">
                  Order #{order.id}
                </h1>
              </div>
            </div>

            <p className="order-details-description">
              Review your order, payment,
              shipment progress, items, and
              delivery information.
            </p>
          </div>
        </header>

        {isPaymentPending && (
          <section className="order-payment-section">
            <div className="order-payment-icon">
              <PaymentIcon />
            </div>

            <div className="order-payment-content">
              <p className="order-section-eyebrow">
                Payment
              </p>

              <h2>
                Payment Required
              </h2>

              <p>
                Your order has been created,
                but payment has not been
                completed yet.
              </p>

              {isPaymentLoading && (
                <p className="order-payment-message">
                  <ClockIcon />
                  Preparing your payment...
                </p>
              )}

              {paymentMessage && (
                <p className="order-payment-message">
                  <CheckIcon />
                  {paymentMessage}
                </p>
              )}

              {paymentError && (
                <p
                  className="order-payment-error"
                  role="alert"
                >
                  <AlertIcon />
                  {paymentError}
                </p>
              )}
            </div>

            <div className="order-payment-action">
              <button
                type="button"
                className="button order-complete-payment-button"
                onClick={
                  handleCompletePayment
                }
                disabled={
                  isPaymentLoading ||
                  isProcessingPayment ||
                  !payment?.id ||
                  payment?.status !==
                    "PENDING"
                }
              >
                <PaymentIcon />

                <span>
                  {isProcessingPayment
                    ? "Processing Payment..."
                    : "Complete Payment"}
                </span>
              </button>
            </div>
          </section>
        )}

        <section
          className={`shipment-tracking-section ${
            isPaymentPending
              ? "shipment-payment-pending"
              : ""
          }`}
        >
          <div className="shipment-section-heading">
            <div className="shipment-section-icon">
              <TruckIcon />
            </div>

            <div>
              <p className="shipment-eyebrow">
                Shipment Tracking
              </p>

              <h2>
                {isPaymentPending
                  ? "Payment Not Completed"
                  : shipment
                    ? getShipmentStatusLabel(
                        shipment.status
                      )
                    : "Preparing Shipment"}
              </h2>

              <p className="shipment-description">
                {isPaymentPending
                  ? "Your order has not been shipped because payment has not been completed."
                  : shipment
                    ? shipmentIsTerminal
                      ? shipmentStatus ===
                        "DELIVERED"
                        ? "Your shipment has reached its destination."
                        : "Your shipment is no longer moving through the delivery process."
                      : "Your shipment is progressing through the delivery process."
                    : "Shipment tracking information is being prepared."}
              </p>
            </div>

            <div
              className={`shipment-status-indicator ${
                isPaymentPending
                  ? "shipment-status-awaiting-payment"
                  : shipment
                    ? `shipment-status-${shipmentStatus.toLowerCase()}`
                    : ""
              }`}
            >
              <span className="shipment-status-dot" />

              <span>
                {isPaymentPending
                  ? "Awaiting Payment"
                  : shipment
                    ? getShipmentStatusLabel(
                        shipment.status
                      )
                    : "Preparing"}
              </span>
            </div>
          </div>

          {isPaymentPending ? (
            <>
              <div className="shipment-pending-notice">
                <div className="shipment-pending-icon">
                  <PaymentIcon />
                </div>

                <div>
                  <strong>
                    Order Not Shipped
                  </strong>

                  <p>
                    Complete your payment first.
                    Shipment tracking will become
                    available after your payment is
                    confirmed and the order enters
                    fulfillment.
                  </p>
                </div>
              </div>

              <div className="shipment-progress shipment-progress-pending">
                {SHIPMENT_STEPS.map(
                  (step) => (
                    <div
                      className="shipment-step shipment-step-disabled"
                      key={
                        step.status
                      }
                    >
                      <div className="shipment-step-marker">
                        <PackageIcon />
                      </div>

                      <div className="shipment-step-content">
                        <strong>
                          {step.label}
                        </strong>

                        <span>
                          Waiting for payment
                        </span>
                      </div>
                    </div>
                  )
                )}
              </div>
            </>
          ) : (
            <>
              {isShipmentLoading && (
                <div
                  className="shipment-loading"
                  aria-live="polite"
                >
                  <span className="shipment-loading-spinner" />

                  <span>
                    Loading shipment tracking...
                  </span>
                </div>
              )}

              {shipmentError && (
                <p
                  className="shipment-error"
                  role="alert"
                >
                  <AlertIcon />
                  {shipmentError}
                </p>
              )}

              {shipment && (
                <>
                  <div className="shipment-progress">
                    {SHIPMENT_STEPS.map(
                      (
                        step,
                        index
                      ) => {
                        const isCompleted =
                          shipmentStepIndex >=
                            0 &&
                          index <=
                            shipmentStepIndex;

                        const isCurrent =
                          shipmentStatus ===
                          step.status;

                        return (
                          <div
                            className={`shipment-step ${
                              isCompleted
                                ? "shipment-step-completed"
                                : ""
                            } ${
                              isCurrent
                                ? "shipment-step-current"
                                : ""
                            }`}
                            key={
                              step.status
                            }
                          >
                            <div className="shipment-step-marker">
                              {isCompleted ? (
                                <CheckIcon />
                              ) : (
                                <span />
                              )}
                            </div>

                            <div className="shipment-step-content">
                              <strong>
                                {
                                  step.label
                                }
                              </strong>

                              <span>
                                {
                                  step.description
                                }
                              </span>
                            </div>
                          </div>
                        );
                      }
                    )}
                  </div>

                  <div className="shipment-information">
                    {shipment.trackingNumber && (
                      <div className="shipment-information-item">
                        <span>
                          Tracking Number
                        </span>

                        <strong>
                          {
                            shipment.trackingNumber
                          }
                        </strong>
                      </div>
                    )}

                    {shipment.carrier && (
                      <div className="shipment-information-item">
                        <span>
                          Carrier
                        </span>

                        <strong>
                          {
                            shipment.carrier
                          }
                        </strong>
                      </div>
                    )}

                    {shipment.shippedAt && (
                      <div className="shipment-information-item">
                        <span>
                          Shipped
                        </span>

                        <strong>
                          {formatShipmentDate(
                            shipment.shippedAt
                          )}
                        </strong>
                      </div>
                    )}

                    {shipment.deliveredAt && (
                      <div className="shipment-information-item">
                        <span>
                          Delivered
                        </span>

                        <strong>
                          {formatShipmentDate(
                            shipment.deliveredAt
                          )}
                        </strong>
                      </div>
                    )}
                  </div>

                  {!shipmentIsTerminal && (
                    <p className="shipment-live-message">
                      <span className="shipment-live-dot" />

                      Shipment status updates
                      automatically.
                    </p>
                  )}
                </>
              )}

              {!shipment &&
                !isShipmentLoading &&
                !shipmentError && (
                  <div className="shipment-empty">
                    <PackageIcon />

                    <p>
                      Shipment tracking will
                      become available once your
                      shipment has been created.
                    </p>
                  </div>
                )}
            </>
          )}
        </section>

        <div className="order-details-layout">
          <section className="order-details-main">

            <div className="order-details-section">
              <div className="order-section-heading">
                <div>
                  <p className="order-section-eyebrow">
                    Order Contents
                  </p>
                </div>
              </div>

              {isReviewsLoading && (
                <p className="order-payment-message">
                  <ClockIcon />
                  Loading your reviews...
                </p>
              )}

              {reviewError && (
                <p
                  className="order-details-error"
                  role="alert"
                >
                  <AlertIcon />
                  {reviewError}
                </p>
              )}

              <div className="order-items">
                {order.items?.map((item, index) => {
                  const itemReview =
                    getReviewForProduct(item.productId);

                  const isReviewFormOpen =
                    reviewFormProductId === item.productId;

                  return (
                    <article
                      className="order-item"
                      key={item.id}
                      style={{
                        "--order-item-delay": `${index * 60}ms`
                      }}
                    >
                      <div className="order-item-content">
                        <div className="order-item-main">
                          <div className="order-item-icon">
                            <ProductsIcon />
                          </div>

                          <div>
                            <h3>{item.productName}</h3>

                            {item.sku && (
                              <p>
                                SKU: {item.sku}
                              </p>
                            )}

                            <p>
                              Quantity: {item.quantity}
                            </p>
                          </div>
                        </div>

                        <div className="order-item-price">
                          <span>
                            {order.currency}{" "}
                            {Number(item.unitPrice).toFixed(2)} each
                          </span>

                          <strong>
                            {order.currency}{" "}
                            {Number(item.lineTotal).toFixed(2)}
                          </strong>
                        </div>
                      </div>

                      <div className="order-item-review">
                        {itemReview ? (
                          <>
                            <div className="order-item-review-header">
                              <span className="order-item-review-label">
                                <ReviewIcon />
                                Your Review
                              </span>

                              <ReviewCard
                                review={itemReview}
                                isOwnReview
                                onEdit={handleEditReview}
                                onDelete={handleDeleteReview}
                                isDeleting={
                                  deletingReviewId === itemReview.id
                                }
                              />
                            </div>

                            {editingReview?.id === itemReview.id && (
                              <ReviewForm
                                review={editingReview}
                                onSubmit={handleReviewSubmit}
                                onCancel={() => {
                                  setEditingReview(null);
                                  setReviewFormProductId(null);
                                  setReviewError("");
                                }}
                                isSubmitting={isReviewSubmitting}
                                error={reviewError}
                              />
                            )}
                          </>
                        ) : (
                          <>
                            {!isReviewFormOpen && (
                              <button
                                type="button"
                                className="order-review-button"
                                onClick={() => {
                                  setReviewError("");
                                  setEditingReview(null);
                                  setReviewFormProductId(
                                    item.productId
                                  );
                                }}
                              >
                                <ReviewIcon />
                                <span>Write a Review</span>
                              </button>
                            )}

                            {isReviewFormOpen && (
                              <div className="order-review-form-wrapper">
                                <div className="order-item-review-header">
                                  <span className="order-item-review-label">
                                    <ReviewIcon />
                                    Review {item.productName}
                                  </span>
                                </div>

                                <ReviewForm
                                  onSubmit={handleReviewSubmit}
                                  onCancel={() => {
                                    setReviewFormProductId(null);
                                    setReviewError("");
                                  }}
                                  isSubmitting={isReviewSubmitting}
                                  error={reviewError}
                                />
                              </div>
                            )}
                          </>
                        )}
                      </div>
                    </article>
                  );
                })}
              </div>
            </div>

            <div className="order-details-section">
              <div className="order-section-heading">
                <div className="order-section-icon">
                  <LocationIcon />
                </div>

                <div>
                  <p className="order-section-eyebrow">
                    Delivery
                  </p>

                  <h2>
                    Shipping Address
                  </h2>
                </div>
              </div>

              <address>
                <strong>
                  {
                    order
                      .shippingAddress
                      ?.recipientName
                  }
                </strong>

                <span>
                  {
                    order
                      .shippingAddress
                      ?.phone
                  }
                </span>

                <span>
                  {
                    order
                      .shippingAddress
                      ?.addressLine1
                  }
                </span>

                {order
                  .shippingAddress
                  ?.addressLine2 && (
                  <span>
                    {
                      order
                        .shippingAddress
                        .addressLine2
                    }
                  </span>
                )}

                <span>
                  {
                    order
                      .shippingAddress
                      ?.city
                  }
                  ,{" "}
                  {
                    order
                      .shippingAddress
                      ?.state
                  }
                </span>

                <span>
                  {
                    order
                      .shippingAddress
                      ?.postalCode
                  }
                </span>

                <span>
                  {
                    order
                      .shippingAddress
                      ?.country
                  }
                </span>
              </address>
            </div>
          </section>

          <aside className="order-details-summary">
            <div className="order-summary-heading">
              <div className="order-summary-icon">
                <OrderIcon />
              </div>

              <div>
                <p className="order-section-eyebrow">
                  Overview
                </p>

                <h2>
                  Order Summary
                </h2>
              </div>
            </div>

            <div className="order-summary-row">
              <span>Status</span>

              <strong>
                {order.status}
              </strong>
            </div>

            <div className="order-summary-row">
              <span>Payment Method</span>

              <strong>
                {
                  order.paymentMethod
                }
              </strong>
            </div>

            {payment && (
              <div className="order-summary-row">
                <span>Payment Status</span>

                <strong>
                  {
                    payment.status
                  }
                </strong>
              </div>
            )}

            {shipment && (
              <div className="order-summary-row">
                <span>Shipment Status</span>

                <strong>
                  {getShipmentStatusLabel(
                    shipment.status
                  )}
                </strong>
              </div>
            )}

            {isPaymentPending && (
              <div className="order-summary-row">
                <span>Shipment</span>

                <strong>
                  Not Shipped
                </strong>
              </div>
            )}

            <div className="order-summary-row order-summary-total">
              <span>Total</span>

              <strong>
                {order.currency}{" "}
                {Number(
                  order.totalAmount
                ).toFixed(2)}
              </strong>
            </div>

            {cancelError && (
              <p
                className="order-details-error"
                role="alert"
              >
                <AlertIcon />
                {cancelError}
              </p>
            )}

            {canCancel && (
              <button
                type="button"
                className="button order-cancel-button"
                onClick={
                  handleCancel
                }
                disabled={
                  isCancelling
                }
              >
                {isCancelling
                  ? "Cancelling..."
                  : "Cancel Order"}
              </button>
            )}
          </aside>
        </div>
      </div>
    </main>
  );
}

export default OrderDetails;

