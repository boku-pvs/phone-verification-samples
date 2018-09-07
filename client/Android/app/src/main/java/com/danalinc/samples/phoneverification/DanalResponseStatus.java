package com.danalinc.samples.phoneverification;

public enum DanalResponseStatus {
    SUCCESS("1"),
    FAILURE("0"),
    SWITCH_TO_FALLBACK("2"),
    ERROR("-1");

    private String value;

    DanalResponseStatus(String value) {
        this.value = value;
    }

    public static DanalResponseStatus fromValue(String value) {
        if (value != null) {
            for (DanalResponseStatus danalResponseStatus : DanalResponseStatus.values()) {
                if (value.equalsIgnoreCase(danalResponseStatus.value)) {
                    return danalResponseStatus;
                }
            }
        }
        return null;
    }
}
