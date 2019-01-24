package com.boku.samples.phoneverification;

public enum BokuResponseStatus {
    SUCCESS("1"),
    FAILURE("0"),
    SWITCH_TO_FALLBACK("2"),
    ERROR("-1");

    private String value;

    BokuResponseStatus(String value) {
        this.value = value;
    }

    public static BokuResponseStatus fromValue(String value) {
        if (value != null) {
            for (BokuResponseStatus bokuResponseStatus : BokuResponseStatus.values()) {
                if (value.equalsIgnoreCase(bokuResponseStatus.value)) {
                    return bokuResponseStatus;
                }
            }
        }
        return null;
    }
}
