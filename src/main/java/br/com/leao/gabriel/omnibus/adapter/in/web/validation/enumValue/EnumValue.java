package br.com.leao.gabriel.omnibus.adapter.in.web.validation.enumValue;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {

  Class<? extends Enum<?>> enumClass();

  String message() default "Value must be one of the accepted options";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}