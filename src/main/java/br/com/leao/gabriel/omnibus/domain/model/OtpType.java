package br.com.leao.gabriel.omnibus.domain.model;

import lombok.Getter;

/**
 * Categorises the purpose of a {@link UserToken}.
 */
@Getter
public enum OtpType {
  ACCOUNT_ACTIVATION("Ative sua conta", "Ative sua conta"),
  PASSWORD_RESET("Redefinição de senha", "Recuperar senha"),
  EMAIL_CHANGE("Alteração de e-mail", "Confirmar novo e-mail");

  private final String emailSubject;
  private final String emailTag;

  OtpType(String emailSubject, String emailTag) {
    this.emailSubject = emailSubject;
    this.emailTag = emailTag;
  }
}
