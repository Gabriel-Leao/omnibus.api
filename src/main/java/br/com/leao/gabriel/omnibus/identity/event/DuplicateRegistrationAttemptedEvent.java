package br.com.leao.gabriel.omnibus.identity.event;

/**
 * Published when a registration attempt targets an email address that is already registered, so
 * the account holder can be notified.
 *
 * @param email the email address that was already registered
 */
public record DuplicateRegistrationAttemptedEvent(String email) {}
