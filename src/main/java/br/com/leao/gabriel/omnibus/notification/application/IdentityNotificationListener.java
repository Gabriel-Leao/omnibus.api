package br.com.leao.gabriel.omnibus.notification.application;

import br.com.leao.gabriel.omnibus.identity.event.CustomerRegisteredEvent;
import br.com.leao.gabriel.omnibus.identity.event.DuplicateRegistrationAttemptedEvent;
import br.com.leao.gabriel.omnibus.identity.event.OtpIssuedEvent;
import br.com.leao.gabriel.omnibus.identity.event.PasswordResetCompletedEvent;
import br.com.leao.gabriel.omnibus.notification.adapter.out.EmailNotifier;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listens to events published by the identity module and sends the corresponding notification.
 *
 * <p>This is the only class in the {@code notification} module that depends on the
 * {@code identity} module. Its dependency is limited to {@code identity.event}; it does not depend
 * on identity's domain model, application services, ports, or persistence.
 *
 * <p>Each identity event contains all information required to send its notification. The
 * notification module therefore never calls back into identity to retrieve additional data.
 *
 * <p>Notification-specific concerns, such as email subjects and templates, are owned by this
 * module. Identity only publishes what happened; notification decides how that event should be
 * communicated.
 */
@Component
@RequiredArgsConstructor
class IdentityNotificationListener {

  private final EmailNotifier emailNotifier;

  @ApplicationModuleListener
  void on(OtpIssuedEvent event) {
    Map<String, Object> variables = new HashMap<>();
    variables.put("subject", event.subject());
    variables.put("tag", event.tag());
    variables.put("name", event.name());
    variables.put("code", event.code());

    emailNotifier.send(event.email(), event.subject(), "identity/otp", variables);
  }

  @ApplicationModuleListener
  void on(CustomerRegisteredEvent event) {
    String subject = "Bem-vindo(a) à Omnibus!";

    Map<String, Object> variables = Map.of("subject", subject, "name", event.name());

    emailNotifier.send(event.email(), subject, "identity/registration-confirmation", variables);
  }

  @ApplicationModuleListener
  void on(DuplicateRegistrationAttemptedEvent event) {
    String subject = "Tentativa de cadastro na Omnibus";

    emailNotifier.send(
        event.email(), subject, "identity/duplicate-registration", Map.of("subject", subject));
  }

  @ApplicationModuleListener
  void on(PasswordResetCompletedEvent event) {
    String subject = "Sua senha foi alterada com sucesso!";

    emailNotifier.send(
        event.email(), subject, "identity/password-reset", Map.of("subject", subject));
  }
}
