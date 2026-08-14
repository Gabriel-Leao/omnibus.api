package br.com.leao.gabriel.omnibus.adapter.in.web.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordConfirmable;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordMatchesValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordMatchesValidatorTest {

  private PasswordMatchesValidator validator;
  private ConstraintValidatorContext context;
  private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
  private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

  @BeforeEach
  void setUp() {
    validator = new PasswordMatchesValidator();
    context = mock(ConstraintValidatorContext.class);
    violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    nodeBuilder =
        mock(
            ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

    when(context.buildConstraintViolationWithTemplate("Passwords do not match"))
        .thenReturn(violationBuilder);
    when(violationBuilder.addPropertyNode("confirmPassword")).thenReturn(nodeBuilder);
  }

  @Test
  void shouldReturnTrueWhenPasswordsMatch() {
    var value = new TestPasswordConfirmable("password123", "password123");

    var result = validator.isValid(value, context);

    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenPasswordsDoNotMatch() {
    var value = new TestPasswordConfirmable("password123", "different");

    var result = validator.isValid(value, context);

    assertFalse(result);
  }

  @Test
  void shouldReturnTrueWhenPasswordIsNull() {
    var value = new TestPasswordConfirmable(null, "password123");

    var result = validator.isValid(value, context);

    assertTrue(result);
  }

  @Test
  void shouldReturnTrueWhenConfirmPasswordIsNull() {
    var value = new TestPasswordConfirmable("password123", null);

    var result = validator.isValid(value, context);

    assertTrue(result);
  }

  @Test
  void shouldAddViolationToConfirmPasswordWhenPasswordsDoNotMatch() {
    var value = new TestPasswordConfirmable("password123", "different");

    var result = validator.isValid(value, context);

    assertFalse(result);

    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate("Passwords do not match");
    verify(violationBuilder).addPropertyNode("confirmPassword");
    verify(nodeBuilder).addConstraintViolation();
  }

  private record TestPasswordConfirmable(String password, String confirmPassword)
      implements PasswordConfirmable {

  }
}