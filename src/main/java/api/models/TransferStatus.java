package api.models;

import lombok.Getter;

@Getter
public enum TransferStatus {
    APPROVED("APPROVED"),
    BLOCKED("BLOCKED"),
    MANUAL_REVIEW_REQUIRED("MANUAL_REVIEW_REQUIRED"),
    VERIFICATION_REQUIRED("VERIFICATION_REQUIRED");

    private final String value;

    TransferStatus(String value) {
        this.value = value;
    }
}