package ui.elements;

import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Condition.value;

public class AmountInput extends BaseElement {

    public AmountInput(SelenideElement element) {
        super(element);
    }

    public void setValue(double amount) {
        element.setValue(String.valueOf(amount));
    }

    public void shouldHaveValue(double expected) {
        element.shouldHave(value(String.valueOf(expected)));
    }
}