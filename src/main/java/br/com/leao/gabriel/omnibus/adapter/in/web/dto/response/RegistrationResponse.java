package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

/**
 * Generic registration confirmation, deliberately uninformative about account existence.
 */
public record RegistrationResponse(String message) {

  public static RegistrationResponse standard() {
    return new RegistrationResponse(
        "If the provided email is valid, further instructions have been sent to it.");
  }
}
