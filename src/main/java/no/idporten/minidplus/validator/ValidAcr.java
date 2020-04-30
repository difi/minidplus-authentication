package no.idporten.minidplus.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AcrValidator.class)
public @interface ValidAcr {
    String message() default "invalid_request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
