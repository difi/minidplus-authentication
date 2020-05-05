package no.idporten.minidplus.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordConstraint.class)
@Documented
public @interface Password {

    String message() default "invalid_password";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}