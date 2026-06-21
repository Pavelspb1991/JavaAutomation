package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS("Name must contain two words with letters only"),
    DEPOSIT_SUCCESSFUL("Successfully deposited"),
    PLEASE_ENTER_VALID_AMOUNT("❌ Please enter a valid amount."),
    DEPOSIT_LIMIT_EXCEEDED("❌ Please deposit less or equal to 5000$");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}