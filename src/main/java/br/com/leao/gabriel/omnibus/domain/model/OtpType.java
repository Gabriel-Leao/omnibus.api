package br.com.leao.gabriel.omnibus.domain.model;

import lombok.Getter;

/**
 * Categorises the purpose of a {@link UserToken}.
 */
@Getter
public enum OtpType {
  ACCOUNT_ACTIVATION("Ative sua conta"),
  PASSWORD_RESET("Redefinição de senha"),
  EMAIL_CHANGE("Alteração de e-mail");

  private final String emailSubject;

  OtpType(String emailSubject) {
    this.emailSubject = emailSubject;
  }
}
