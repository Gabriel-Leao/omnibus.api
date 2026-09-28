package br.com.leao.gabriel.omnibus.identity.event;

/**
 * Published after a customer's password has been successfully changed via the password reset
 * flow, so the account holder can be notified.
 *
 * @param email the email address of the customer whose password was reset
 */
public record PasswordResetCompletedEvent(String email) {}
