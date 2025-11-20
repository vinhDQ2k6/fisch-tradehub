package com.fisch_tradehub.tradehub_core.entity;

public enum BillStatus {
    PENDING_PAYMENT(0),
    PROCESSING(1),
    COMPLETED(2);

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
}
