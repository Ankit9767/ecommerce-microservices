const RAZORPAY_SCRIPT_URL =
  "https://checkout.razorpay.com/v1/checkout.js";

let razorpayScriptPromise = null;

function loadRazorpayScript() {
  if (window.Razorpay) {
    return Promise.resolve(true);
  }

  if (razorpayScriptPromise) {
    return razorpayScriptPromise;
  }

  razorpayScriptPromise = new Promise(
    (resolve, reject) => {
      const existingScript =
        document.querySelector(
          `script[src="${RAZORPAY_SCRIPT_URL}"]`
        );

      if (existingScript) {
        existingScript.addEventListener(
          "load",
          () => resolve(true)
        );

        existingScript.addEventListener(
          "error",
          () => {
            reject(
              new Error(
                "Unable to load Razorpay Checkout."
              )
            );
          }
        );

        return;
      }

      const script =
        document.createElement("script");

      script.src = RAZORPAY_SCRIPT_URL;
      script.async = true;

      script.onload = () => {
        resolve(true);
      };

      script.onerror = () => {
        razorpayScriptPromise = null;

        reject(
          new Error(
            "Unable to load Razorpay Checkout."
          )
        );
      };

      document.body.appendChild(script);
    }
  );

  return razorpayScriptPromise;
}

function toRazorpayAmount(amount) {
  return Math.round(
    Number(amount) * 100
  );
}

async function openRazorpayCheckout({
  key,
  amount,
  currency,
  orderId,
  name = "EcommerceHub",
  description = "Order Payment",
  prefill,
  notes,
  onSuccess,
  onFailure,
  onDismiss
}) {
  await loadRazorpayScript();

  if (!window.Razorpay) {
    throw new Error(
      "Razorpay Checkout is unavailable."
    );
  }

  const options = {
    key,
    amount,
    currency,
    name,
    description,
    order_id: orderId,
    prefill,
    notes,

    handler: onSuccess,

    modal: {
      ondismiss: onDismiss
    }
  };

  const razorpay =
    new window.Razorpay(options);

  razorpay.on(
    "payment.failed",
    (response) => {
      if (onFailure) {
        onFailure(response);
      }
    }
  );

  razorpay.open();
}

export {
  loadRazorpayScript,
  toRazorpayAmount,
  openRazorpayCheckout
};