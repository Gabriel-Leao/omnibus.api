package br.com.leao.gabriel.omnibus.domain.model;

/**
 * Categorizes the purpose of a {@link UserToken}.
 */
public enum OtpType {

  ACCOUNT_ACTIVATION("Ative sua conta"),
  PASSWORD_RESET("Redefinição de senha"),
  EMAIL_CHANGE("Alteração de e-mail");

  private final String emailSubject;

  OtpType(String emailSubject) {
    this.emailSubject = emailSubject;
  }

  public String getEmailSubject() {
    return emailSubject;
  }
}
