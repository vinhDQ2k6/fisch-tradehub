package com.fisch_tradehub.tradehub_core.entity;

import java.util.EnumSet;
import java.util.Set;

public enum BillStatus {
  PENDING_PAYMENT(0),
  PROCESSING(1),
  COMPLETED(2),
  CANCELLED(3);

  private final int value;

  BillStatus(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static BillStatus fromValue(int value) {
    for (BillStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown BillStatus value: " + value);
  }

  /**
   * Get valid next statuses from current status.
   * Status can only move forward or to CANCELLED.
   *
   * Flow: PENDING_PAYMENT → PROCESSING → COMPLETED
   *                ↓              ↓
   *            CANCELLED      CANCELLED
   */
  public Set<BillStatus> getValidTransitions() {
    return switch (this) {
      case PENDING_PAYMENT -> EnumSet.of(PROCESSING, CANCELLED);
      case PROCESSING -> EnumSet.of(COMPLETED, CANCELLED);
      case COMPLETED -> EnumSet.noneOf(BillStatus.class); // Terminal state
      case CANCELLED -> EnumSet.noneOf(BillStatus.class); // Terminal state
    };
  }

  /**
   * Check if transition to target status is valid.
   */
  public boolean canTransitionTo(BillStatus target) {
    return getValidTransitions().contains(target);
  }
}
