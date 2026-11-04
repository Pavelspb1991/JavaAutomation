package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FraudCheckMock {
    int port() default 8080;
    String endpoint() default "/fraud-check";
    String status() default "SUCCESS";
    String decision() default "APPROVED";
    double riskScore() default 0.1;
    String reason() default "No fraud detected";
    boolean requiresManualReview() default false;
    boolean additionalVerificationRequired() default false;
}