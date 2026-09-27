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

  /*
   * IMPORTANT:
   * This loading state is ONLY for the order.
   * Review loading never changes this state.
   */
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

  /*
   * Reviews
   */
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

  /*
   * ============================================================
   * LOAD ORDER
   * ============================================================
   *
   * This is intentionally independent from reviews.
   *
   * The 15 second timeout guarantees that the page does not
   * remain on "Loading order..." forever if getOrder() hangs.
   */
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
        console.log("Loading order:", id);

        const response = await getOrder(id);

        console.log("Order response:", response);

        if (!isMounted) {
          return;
        }

        setOrder(response);
      } catch (requestError) {
        console.error(
          "Order loading failed:",
          requestError
        );

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
          console.log(
            "Setting isLoading(false)"
          );

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

  /*
   * ============================================================
   * LOAD MY REVIEWS
   * ============================================================
   *
   * IMPORTANT:
   * This does NOT touch isLoading.
   *
   * Therefore a slow/failing getMyReviews() request cannot
   * make the entire OrderDetails page display "Loading order...".
   */
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

        console.error(
          "Review loading failed:",
          requestError
        );

        setReviewError(
          requestError?.message ||
            "Unable to load your reviews."
        );

        /*
         * Important:
         * Even if reviews fail, the order page remains usable.
         */
        setMyReviews([]);
      } finally {
        if (isMounted) {
          setIsReviewsLoading(false);
        }
      }
    }

    /*
     * Review loading waits only for authentication.
     * It does NOT wait for the order request.
     */
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

  /*
   * ============================================================
   * FIND REVIEW FOR PRODUCT
   * ============================================================
   */
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

  /*
   * ============================================================
   * SUBMIT / UPDATE REVIEW
   * ============================================================
   */
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

      /*
       * Update existing review
       */
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
      }

      /*
       * Create new review
       */
      else {
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
      console.error(
        "Review save failed:",
        requestError
      );

      setReviewError(
        requestError?.message ||
          "Unable to save your review."
      );
    } finally {
      setIsReviewSubmitting(false);
    }
  };

  /*
   * ============================================================
   * EDIT REVIEW
   * ============================================================
   */
  const handleEditReview = (
    review
  ) => {
    setReviewError("");

    setEditingReview(review);

    setReviewFormProductId(
      review.productId
    );
  };

  /*
   * ============================================================
   * DELETE REVIEW
   * ============================================================
   */
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

    setDeletingReviewId(
      review.id
    );

    setReviewError("");

    try {
      await deleteReview(
        review.id
      );

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
      console.error(
        "Review delete failed:",
        requestError
      );

      setReviewError(
        requestError?.message ||
          "Unable to delete your review."
      );
    } finally {
      setDeletingReviewId(null);
    }
  };

  /*
   * ============================================================
   * LOAD SHIPMENT
   * ============================================================
   */
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
          await trackShipmentByOrderId(
            id
          );

        setShipment(response);

        return response;
      } catch (requestError) {
        if (
          requestError?.status ===
          404
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

  /*
   * ============================================================
   * SHIPMENT POLLING
   * ============================================================
   */
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
              await loadShipment(
                false
              );

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

  /*
   * ============================================================
   * FIND PAYMENT
   * ============================================================
   */
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

  /*
   * ============================================================
   * DISCOVER PAYMENT
   * ============================================================
   */
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

  /*
   * ============================================================
   * WAIT FOR PAYMENT RESULT
   * ============================================================
   */
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

  /*
   * ============================================================
   * COMPLETE PAYMENT
   * ============================================================
   */
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

  /*
   * ============================================================
   * CANCEL ORDER
   * ============================================================
   */
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

  /*
   * ============================================================
   * PAGE LOADING
   * ============================================================
   *
   * Only authentication + order loading control this screen.
   *
   * Review loading does NOT appear here.
   */
  if (
    isAuthLoading ||
    isLoading
  ) {
    return (
      <main className="page">
        <div className="container">
          <p className="order-details-state">
            Loading order...
          </p>
        </div>
      </main>
    );
  }

  /*
   * ============================================================
   * NOT AUTHENTICATED
   * ============================================================
   */
  if (!isAuthenticated) {
    return (
      <main className="page">
        <div className="container">
          <div className="order-details-state">
            <h1 className="page-title">
              Order Details
            </h1>

            <p>
              Please sign in to view
              your order.
            </p>

            <Link
              className="button"
              to="/login"
            >
              Sign In
            </Link>
          </div>
        </div>
      </main>
    );
  }

  /*
   * ============================================================
   * ORDER ERROR
   * ============================================================
   */
  if (error) {
    return (
      <main className="page">
        <div className="container">
          <div className="order-details-state">
            <p className="order-details-error">
              {error}
            </p>

            <Link
              className="button"
              to="/orders"
            >
              Back to Orders
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
    <main className="page">
      <div className="container">

        {/* =====================================================
            HEADER
        ===================================================== */}

        <div className="order-details-header">
          <div>
            <Link
              className="order-back-link"
              to="/orders"
            >
              ← Back to Orders
            </Link>

            <h1 className="page-title">
              Order #{order.id}
            </h1>
          </div>

          <span
            className={`order-details-status ${
              isPaymentPending
                ? "order-details-status-pending"
                : ""
            }`}
          >
            {order.status}
          </span>
        </div>

        {/* =====================================================
            PAYMENT
        ===================================================== */}

        {isPaymentPending && (
          <section className="order-payment-section">
            <div className="order-payment-content">
              <h2>
                Payment Required
              </h2>

              <p>
                Your order has been
                created, but payment has
                not been completed yet.
              </p>

              {isPaymentLoading && (
                <p className="order-payment-message">
                  Preparing your payment...
                </p>
              )}

              {paymentMessage && (
                <p className="order-payment-message">
                  {paymentMessage}
                </p>
              )}

              {paymentError && (
                <p
                  className="order-payment-error"
                  role="alert"
                >
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
                {isProcessingPayment
                  ? "Processing Payment..."
                  : "Complete Payment"}
              </button>
            </div>
          </section>
        )}

        {/* =====================================================
            SHIPMENT
        ===================================================== */}

        <section
          className={`shipment-tracking-section ${
            isPaymentPending
              ? "shipment-payment-pending"
              : ""
          }`}
        >
          {isPaymentPending ? (
            <>
              <div className="shipment-tracking-header">
                <div>
                  <p className="shipment-eyebrow">
                    Shipment Tracking
                  </p>

                  <h2>
                    Payment Not Completed
                  </h2>

                  <p className="shipment-description">
                    Your order has not
                    been shipped because
                    payment has not been
                    completed.
                  </p>
                </div>

                <div className="shipment-status-indicator shipment-status-awaiting-payment">
                  <span className="shipment-status-dot" />

                  <span>
                    Awaiting Payment
                  </span>
                </div>
              </div>

              <div className="shipment-pending-notice">
                <div className="shipment-pending-icon">
                  !
                </div>

                <div>
                  <strong>
                    Order Not Shipped
                  </strong>

                  <p>
                    Complete your
                    payment first.
                    Shipment tracking
                    will become
                    available after
                    your payment is
                    confirmed and the
                    order enters
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
                        <span />
                      </div>

                      <div className="shipment-step-content">
                        <strong>
                          {
                            step.label
                          }
                        </strong>

                        <span>
                          Waiting for
                          payment
                        </span>
                      </div>
                    </div>
                  )
                )}
              </div>
            </>
          ) : (
            <>
              <div className="shipment-tracking-header">
                <div>
                  <p className="shipment-eyebrow">
                    Shipment Tracking
                  </p>

                  <h2>
                    {shipment
                      ? getShipmentStatusLabel(
                          shipment.status
                        )
                      : "Preparing Shipment"}
                  </h2>

                  <p className="shipment-description">
                    {shipment
                      ? shipmentIsTerminal
                        ? shipmentStatus ===
                          "DELIVERED"
                          ? "Your shipment has reached its destination."
                          : "Your shipment is no longer moving through the delivery process."
                        : "Your shipment is progressing through the delivery process."
                      : "Shipment tracking information is being prepared."}
                  </p>
                </div>

                {shipment && (
                  <div
                    className={`shipment-status-indicator shipment-status-${shipmentStatus.toLowerCase()}`}
                    aria-label={`Shipment status: ${getShipmentStatusLabel(
                      shipment.status
                    )}`}
                  >
                    <span className="shipment-status-dot" />

                    <span>
                      {getShipmentStatusLabel(
                        shipment.status
                      )}
                    </span>
                  </div>
                )}
              </div>

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
                              {isCompleted
                                ? "✓"
                                : ""}
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
                          Tracking
                          Number
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

                      Shipment status
                      updates
                      automatically.
                    </p>
                  )}
                </>
              )}

              {!shipment &&
                !isShipmentLoading &&
                !shipmentError && (
                  <div className="shipment-empty">
                    <p>
                      Shipment tracking
                      will become
                      available once
                      your shipment has
                      been created.
                    </p>
                  </div>
                )}
            </>
          )}
        </section>

        {/* =====================================================
            ORDER CONTENT
        ===================================================== */}

        <div className="order-details-layout">
          <section className="order-details-main">

            {/* =================================================
                ITEMS + REVIEWS
            ================================================= */}

            <div className="order-details-section">
              <h2>Items</h2>

              {/* Review loading does NOT block the order. */}
              {isReviewsLoading && (
                <p className="order-payment-message">
                  Loading your reviews...
                </p>
              )}

              {reviewError && (
                <p
                  className="order-details-error"
                  role="alert"
                >
                  {reviewError}
                </p>
              )}

              <div className="order-items">
                {order.items?.map(
                  (item) => {
                    const itemReview =
                      getReviewForProduct(
                        item.productId
                      );

                    const isReviewFormOpen =
                      reviewFormProductId ===
                      item.productId;

                    return (
                      <article
                        className="order-item"
                        key={item.id}
                      >
                        {/* ===============================
                            ITEM INFORMATION
                        =============================== */}

                        <div className="order-item-content">
                          <div>
                            <h3>
                              {
                                item.productName
                              }
                            </h3>

                            {item.sku && (
                              <p>
                                SKU:{" "}
                                {
                                  item.sku
                                }
                              </p>
                            )}

                            <p>
                              Quantity:{" "}
                              {
                                item.quantity
                              }
                            </p>
                          </div>

                          <div className="order-item-price">
                            <span>
                              {
                                order.currency
                              }{" "}
                              {Number(
                                item.unitPrice
                              ).toFixed(
                                2
                              )}{" "}
                              each
                            </span>

                            <strong>
                              {
                                order.currency
                              }{" "}
                              {Number(
                                item.lineTotal
                              ).toFixed(
                                2
                              )}
                            </strong>
                          </div>
                        </div>

                        {/* ===============================
                            REVIEW
                        =============================== */}

                        <div className="order-item-review">
                          {itemReview ? (
                            <>
                              <div className="order-item-review-header">
                                <span className="order-item-review-label">
                                  Your
                                  Review
                                </span>

                                <ReviewCard
                                  review={
                                    itemReview
                                  }
                                  isOwnReview
                                  onEdit={
                                    handleEditReview
                                  }
                                  onDelete={
                                    handleDeleteReview
                                  }
                                  isDeleting={
                                    deletingReviewId ===
                                    itemReview.id
                                  }
                                />
                              </div>

                              {editingReview?.id ===
                                itemReview.id && (
                                <ReviewForm
                                  review={
                                    editingReview
                                  }
                                  onSubmit={
                                    handleReviewSubmit
                                  }
                                  onCancel={() => {
                                    setEditingReview(
                                      null
                                    );

                                    setReviewFormProductId(
                                      null
                                    );

                                    setReviewError(
                                      ""
                                    );
                                  }}
                                  isSubmitting={
                                    isReviewSubmitting
                                  }
                                  error={
                                    reviewError
                                  }
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
                                    setReviewError(
                                      ""
                                    );

                                    setEditingReview(
                                      null
                                    );

                                    setReviewFormProductId(
                                      item.productId
                                    );
                                  }}
                                >
                                  Write a Review
                                </button>
                              )}

                              {isReviewFormOpen && (
                                <div className="order-review-form-wrapper">
                                  <div className="order-item-review-header">
                                    <span className="order-item-review-label">
                                      Review{" "}
                                      {
                                        item.productName
                                      }
                                    </span>
                                  </div>

                                  <ReviewForm
                                    onSubmit={
                                      handleReviewSubmit
                                    }
                                    onCancel={() => {
                                      setReviewFormProductId(
                                        null
                                      );

                                      setReviewError(
                                        ""
                                      );
                                    }}
                                    isSubmitting={
                                      isReviewSubmitting
                                    }
                                    error={
                                      reviewError
                                    }
                                  />
                                </div>
                              )}
                            </>
                          )}
                        </div>
                      </article>
                    );
                  }
                )}
              </div>
            </div>

            {/* =================================================
                SHIPPING ADDRESS
            ================================================= */}

            <div className="order-details-section">
              <h2>
                Shipping Address
              </h2>

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

          {/* ===================================================
              ORDER SUMMARY
          =================================================== */}

          <aside className="order-details-summary">
            <h2>
              Order Summary
            </h2>

            <div className="order-summary-row">
              <span>
                Status
              </span>

              <strong>
                {order.status}
              </strong>
            </div>

            <div className="order-summary-row">
              <span>
                Payment Method
              </span>

              <strong>
                {
                  order.paymentMethod
                }
              </strong>
            </div>

            {payment && (
              <div className="order-summary-row">
                <span>
                  Payment Status
                </span>

                <strong>
                  {
                    payment.status
                  }
                </strong>
              </div>
            )}

            {shipment && (
              <div className="order-summary-row">
                <span>
                  Shipment Status
                </span>

                <strong>
                  {getShipmentStatusLabel(
                    shipment.status
                  )}
                </strong>
              </div>
            )}

            {isPaymentPending && (
              <div className="order-summary-row">
                <span>
                  Shipment
                </span>

                <strong>
                  Not Shipped
                </strong>
              </div>
            )}

            <div className="order-summary-row order-summary-total">
              <span>
                Total
              </span>

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
