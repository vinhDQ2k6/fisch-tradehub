package com.fisch_tradehub.tradehub_core.common;

/**
 * Application-wide constants.
 */
public final class Constants {

  private Constants() {
    // Prevent instantiation
  }

  // User roles
  public static final String ROLE_USER = "ROLE_USER";
  public static final String ROLE_STAFF = "ROLE_STAFF";
  public static final String ROLE_ADMIN = "ROLE_ADMIN";

  // Error messages
  public static final String USER_NOT_FOUND = "User not found";
  public static final String BILL_NOT_FOUND = "Bill not found";
  public static final String FISH_NOT_FOUND = "Fish not found";
  public static final String CART_EMPTY = "Cart is empty";
  public static final String CART_ITEM_NOT_FOUND = "Cart item not found";

  public static final String UNAUTHORIZED_BILL_ACCESS =
    "Unauthorized access to bill";
  public static final String BILL_NOT_PENDING =
    "Bill is not in pending payment status";
  public static final String BILL_NOT_PROCESSING =
    "Only processing bills can be completed";
  public static final String BILL_CANNOT_CANCEL =
    "Only pending bills can be cancelled";
  public static final String INVALID_STATUS_TRANSITION =
    "Invalid status transition from %s to %s";

  public static final String USERNAME_EXISTS = "Username already exists";
  public static final String EMAIL_EXISTS = "Email already exists";

  // Admin User Management Messages
  public static final String CANNOT_DISABLE_SELF = "Cannot disable your own account";
  public static final String INVALID_ROLE = "Invalid role specified";

  // Password Reset Messages
  public static final String TOKEN_NOT_FOUND = "Invalid or expired reset token";
  public static final String TOKEN_EXPIRED = "Reset token has expired";
  public static final String TOKEN_ALREADY_USED = "Reset token has already been used";
  public static final String PASSWORD_RESET_EMAIL_SENT = "If the email exists, a reset link has been sent";
}
