package br.com.leao.gabriel.omnibus.adapter.in.web.validation.minimunAge;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = MinimumAgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinimumAge {

  int value() default 18;

  String message() default "User must be at least {value} years old";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
