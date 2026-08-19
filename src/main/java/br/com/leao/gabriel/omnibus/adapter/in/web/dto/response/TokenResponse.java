package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

/**
 * Response payload carrying a signed JWT access token.
 */
public record TokenResponse(String accessToken) {

}