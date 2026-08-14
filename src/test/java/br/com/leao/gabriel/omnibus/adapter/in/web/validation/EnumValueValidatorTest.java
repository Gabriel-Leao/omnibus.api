package br.com.leao.gabriel.omnibus.adapter.in.web.validation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.adapter.in.web.validation.enumvalue.EnumValue;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.enumvalue.EnumValueValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnumValueValidatorTest {

  private EnumValueValidator validator;

  @Mock private EnumValue annotation;

  @Mock private ConstraintValidatorContext context;

  @Mock private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @BeforeEach
  void setUp() {
    validator = new EnumValueValidator();

    doReturn(CustomerStatus.class).when(annotation).enumClass();

    validator.initialize(annotation);
  }

  @Test
  void shouldReturnTrueWhenValueIsValid() {
    var result = validator.isValid("ACTIVE", context);

    assertThat(result).isTrue();

    verify(context, never()).disableDefaultConstraintViolation();
  }

  @Test
  void shouldReturnFalseWhenValueIsInvalid() {
    when(context.buildConstraintViolationWithTemplate("Must be one of: ACTIVE, INACTIVE"))
        .thenReturn(violationBuilder);

    var result = validator.isValid("DELETED", context);

    assertThat(result).isFalse();

    verify(context).disableDefaultConstraintViolation();

    verify(context).buildConstraintViolationWithTemplate("Must be one of: ACTIVE, INACTIVE");

    verify(violationBuilder).addConstraintViolation();
  }

  @Test
  void shouldReturnTrueWhenValueIsNull() {
    var result = validator.isValid(null, context);

    assertThat(result).isTrue();

    verifyNoInteractions(context);
  }

  enum CustomerStatus {
    ACTIVE,
    INACTIVE
  }
}
