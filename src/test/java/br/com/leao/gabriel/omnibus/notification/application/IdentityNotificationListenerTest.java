package br.com.leao.gabriel.omnibus.notification.application;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import br.com.leao.gabriel.omnibus.identity.event.CustomerRegisteredEvent;
import br.com.leao.gabriel.omnibus.identity.event.DuplicateRegistrationAttemptedEvent;
import br.com.leao.gabriel.omnibus.identity.event.OtpIssuedEvent;
import br.com.leao.gabriel.omnibus.identity.event.PasswordResetCompletedEvent;
import br.com.leao.gabriel.omnibus.notification.adapter.out.EmailNotifier;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link IdentityNotificationListener}.
 *
 * <p>These tests only construct plain {@code identity.event} records and verify the resulting
 * {@link EmailNotifier} call — exactly what the class itself is limited to. No identity domain
 * model, port, or persistence type is used anywhere here, which is itself evidence that the
 * listener holds to its documented boundary.
 */
@ExtendWith(MockitoExtension.class)
class IdentityNotificationListenerTest {

  @Mock private EmailNotifier emailNotifier;

  private IdentityNotificationListener listener;

  @BeforeEach
  void setUp() {
    listener = new IdentityNotificationListener(emailNotifier);
  }

  @Test
  void shouldSendOtpEmailWithSubjectTagNameAndCode() {
    var event =
        new OtpIssuedEvent(
            "gabriel@example.com", "Gabriel Leão", "482913", "Ative sua conta", "Ative sua conta");

    listener.on(event);

    verify(emailNotifier)
        .send(
            eq("gabriel@example.com"),
            eq("Ative sua conta"),
            eq("identity/otp"),
            eq(
                Map.of(
                    "subject", "Ative sua conta",
                    "tag", "Ative sua conta",
                    "name", "Gabriel Leão",
                    "code", "482913")));
  }

  @Test
  void shouldSendWelcomeEmailOnCustomerRegistered() {
    var event = new CustomerRegisteredEvent("gabriel@example.com", "Gabriel Leão");

    listener.on(event);

    verify(emailNotifier)
        .send(
            eq("gabriel@example.com"),
            eq("Bem-vindo(a) à Omnibus!"),
            eq("identity/registration-confirmation"),
            eq(Map.of("subject", "Bem-vindo(a) à Omnibus!", "name", "Gabriel Leão")));
  }

  @Test
  void shouldSendDuplicateRegistrationNotice() {
    var event = new DuplicateRegistrationAttemptedEvent("gabriel@example.com");

    listener.on(event);

    verify(emailNotifier)
        .send(
            eq("gabriel@example.com"),
            eq("Tentativa de cadastro na Omnibus"),
            eq("identity/duplicate-registration"),
            eq(Map.of("subject", "Tentativa de cadastro na Omnibus")));
  }

  @Test
  void shouldSendPasswordResetCompletedNotice() {
    var event = new PasswordResetCompletedEvent("gabriel@example.com");

    listener.on(event);

    verify(emailNotifier)
        .send(
            eq("gabriel@example.com"),
            eq("Sua senha foi alterada com sucesso!"),
            eq("identity/password-reset"),
            eq(Map.of("subject", "Sua senha foi alterada com sucesso!")));
  }
}
