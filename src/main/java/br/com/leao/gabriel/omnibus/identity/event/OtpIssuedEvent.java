package br.com.leao.gabriel.omnibus.identity.event;

/**
 * Published when a one-time password has been issued to a customer and is ready to be delivered.
 *
 * <p>{@code subject} and {@code tag} are already resolved to plain strings here — they come from
 * {@code OtpType.getEmailSubject()} / {@code getEmailTag()}, which only identity is allowed to
 * call. Listeners outside this module never need to know {@code OtpType} exists.
 *
 * @param email the recipient's email address
 * @param name  the recipient's display name
 * @param code  the plain-text OTP
 * @param subject the email subject line for this OTP's purpose
 * @param tag     a short label identifying this OTP's purpose, shown in the email body
 */
public record OtpIssuedEvent(String email, String name, String code, String subject, String tag) {}
