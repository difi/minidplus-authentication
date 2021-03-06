package no.idporten.minidplus.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = {FIELD, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {URIValidator.class})
public @interface ValidSSN {
    String message() default "{auth.ui.usererror.format.ssn}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}