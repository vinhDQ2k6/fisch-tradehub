import { HTTP_STATUS, ERROR_MESSAGES, TOAST_DURATION } from "@/common/constants";

/**
 * Error handling utilities for displaying user-friendly error messages.
 */

/**
 * Map HTTP status code to user-friendly message.
 * @param {Object} err - Error object
 * @param {number} [err.status] - HTTP status code
 * @param {string} [err.name] - Error name
 * @param {string} [err.message] - Error message
 * @returns {string|null} User-friendly message or null
 */
function mapFriendlyMessage(err) {
    const status = err?.status;
    if (!status) {
        // Network/JS errors without HTTP status
        const msg = (err && err.message) || String(err || "");
        if (
            msg.toLowerCase().includes("network") ||
            err?.name === "TypeError"
        ) {
            return ERROR_MESSAGES.NETWORK;
        }
        if (err?.name === "AbortError") return "Request was canceled.";
        return null;
    }
    switch (status) {
        case HTTP_STATUS.BAD_REQUEST:
            return ERROR_MESSAGES.VALIDATION_FAILED;
        case HTTP_STATUS.UNAUTHORIZED:
            return ERROR_MESSAGES.INVALID_CREDENTIALS;
        case HTTP_STATUS.FORBIDDEN:
            return ERROR_MESSAGES.PERMISSION_DENIED;
        case HTTP_STATUS.NOT_FOUND:
            return ERROR_MESSAGES.NOT_FOUND;
        case HTTP_STATUS.CONFLICT:
            return ERROR_MESSAGES.ALREADY_EXISTS;
        case 410:
            return "The resource is no longer available.";
        case 412:
            return "Precondition failed. Your data may be outdated.";
        case 415:
            return "Unsupported media type.";
        case 422:
            return ERROR_MESSAGES.VALIDATION_FAILED;
        case 423:
            return "Account locked. Please contact support.";
        case 425:
            return "Please wait and try again.";
        case 429:
            return "Too many attempts. Please try again later.";
        case 451:
            return "Unavailable due to legal reasons.";
        case 502:
            return "Bad gateway from upstream service.";
        case 503:
            return "Service unavailable. Please try again shortly.";
        case 504:
            return "Gateway timeout. The server took too long.";
        default:
            if (status >= 500) return ERROR_MESSAGES.SERVER_ERROR;
            return null;
    }
}

/**
 * Extract validation message from error data.
 * @param {Object} data - Error response data
 * @returns {string|null} Validation message or null
 */
function extractValidationMessage(data) {
    if (!data) return null;
    // Common API shapes: { errors: [{ message }, ...] } or { errors: ["msg", ...] }
    if (Array.isArray(data.errors) && data.errors.length) {
        const first = data.errors[0];
        return typeof first === "string" ? first : first?.message || null;
    }
    // Another common shape: { error: "..." } or { message: "..." }
    return data.error || data.message || null;
}

/**
 * Display error toast notification.
 * 
 * @param {Object} toast - PrimeVue toast service
 * @param {Object|string} err - Error object or message
 * @param {Object} [opts={}] - Display options
 * @param {string} [opts.message] - Override error message
 * @param {string} [opts.summary='Error'] - Toast summary/title
 * @param {string} [opts.severity='error'] - Toast severity
 * @param {number} [opts.life] - Toast duration in milliseconds
 * 
 * @example
 * try {
 *   await apiCall();
 * } catch (err) {
 *   showError(toast, err);
 * }
 */
export function showError(toast, err, opts = {}) {
    const mapped = mapFriendlyMessage(err);
    const dataMsg = extractValidationMessage(err?.data);
    const detail =
        opts.message ||
        dataMsg ||
        mapped ||
        err?.message ||
        (typeof err === "string" ? err : ERROR_MESSAGES.UNEXPECTED);
    const summary = opts.summary || "Error";
    const severity = opts.severity || "error";
    toast.add({ severity, summary, detail, life: opts.life ?? TOAST_DURATION.NORMAL });
}

/**
 * Display success toast notification.
 * 
 * @param {Object} toast - PrimeVue toast service
 * @param {string} message - Success message
 * @param {Object} [opts={}] - Display options
 * @param {string} [opts.summary='Success'] - Toast summary/title
 * @param {string} [opts.severity='success'] - Toast severity
 * @param {number} [opts.life] - Toast duration in milliseconds
 * 
 * @example
 * showSuccess(toast, 'Item added to cart!');
 */
export function showSuccess(toast, message, opts = {}) {
    const summary = opts.summary || "Success";
    const severity = opts.severity || "success";
    toast.add({ severity, summary, detail: message, life: opts.life ?? TOAST_DURATION.SHORT });
}
