package br.com.leao.gabriel.omnibus.adapter.in.web.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldReturnBadRequestForIllegalArgument() {
    var response =
        handler.handleIllegalArgument(
            new IllegalArgumentException("Invalid input"), new MockHttpServletRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().status()).isEqualTo(400);
    assertThat(response.getBody().message()).isEqualTo("Invalid input");
    assertThat(response.getBody().timestamp()).isNotNull();
  }

  @Test
  void shouldHideUnexpectedExceptionDetails() {
    var response =
        handler.handleUnexpected(
            new RuntimeException("sensitive detail"), new MockHttpServletRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
  }
}
