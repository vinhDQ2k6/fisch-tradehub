function mapFriendlyMessage(err) {
    const status = err?.status;
    if (!status) {
        // Network/JS errors without HTTP status
        const msg = (err && err.message) || String(err || "");
        if (
            msg.toLowerCase().includes("network") ||
            err?.name === "TypeError"
        ) {
            return "Network error. Please check your connection.";
        }
        if (err?.name === "AbortError") return "Request was canceled.";
        return null;
    }
    switch (status) {
        case 400:
            return "Invalid request. Please check your input.";
        case 401:
            return "Invalid credentials or session expired.";
        case 403:
            return "You don't have permission to perform this action.";
        case 404:
            return "Resource not found.";
        case 409:
            return "Conflict: the resource already exists.";
        case 410:
            return "The resource is no longer available.";
        case 412:
            return "Precondition failed. Your data may be outdated.";
        case 415:
            return "Unsupported media type.";
        case 422:
            return "Validation failed. Please review the form.";
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
            if (status >= 500) return "Server error. Please try again later.";
            return null;
    }
}

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

export function showError(toast, err, opts = {}) {
    const mapped = mapFriendlyMessage(err);
    const dataMsg = extractValidationMessage(err?.data);
    const detail =
        opts.message ||
        dataMsg ||
        mapped ||
        err?.message ||
        (typeof err === "string" ? err : "Unexpected error");
    const summary = opts.summary || "Error";
    const severity = opts.severity || "error";
    toast.add({ severity, summary, detail, life: opts.life ?? 4000 });
}

export function showSuccess(toast, message, opts = {}) {
    const summary = opts.summary || "Success";
    const severity = opts.severity || "success";
    toast.add({ severity, summary, detail: message, life: opts.life ?? 3000 });
}
