package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.domain.exception.DailyTokenLimitExceededException;
import br.com.leao.gabriel.omnibus.domain.model.TokenType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuanceRateLimiterPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Centralizes verification code issuance: enforces the daily rate limit, generates and persists the
 * token, and returns the plain-text code for the caller to send by email. Shared by registration
 * and resend flows to avoid duplicating this logic.
 */
@Component
@RequiredArgsConstructor
public class VerificationCodeIssuer {

  private static final int MAX_TOKENS_PER_DAY = 3;
  private static final long DEFAULT_EXPIRATION_MINUTES = 15;

  private final OtpGeneratorPort otpGenerator;
  private final UserTokenRepositoryPort userTokenRepository;
  private final TokenIssuanceRateLimiterPort rateLimiter;

  /**
   * Issues a new activation or password-reset code for the given user, enforcing the daily limit.
   *
   * @param userId the account the code is for
   * @param type   the token type ({@code ACCOUNT_ACTIVATION} or {@code PASSWORD_RESET})
   * @return the plain-text code, to be sent by email
   * @throws DailyTokenLimitExceededException if the daily issuance limit has been reached
   */
  public String issue(String userId, TokenType type) {
    return issue(userId, type, DEFAULT_EXPIRATION_MINUTES);
  }

  /**
   * Issues a new verification code with a custom expiration, enforcing the daily limit.
   *
   * @param userId            the account the code is for
   * @param type              the token type
   * @param expirationMinutes how many minutes until the code expires
   * @return the plain-text code, to be sent by email
   */
  public String issue(String userId, TokenType type, long expirationMinutes) {
    long count = rateLimiter.incrementAndGet(userId, type);
    if (count > MAX_TOKENS_PER_DAY) {
      throw new DailyTokenLimitExceededException();
    }

    String code = otpGenerator.generateCode();
    String codeHash = otpGenerator.hash(code);
    UserToken token =
        UserToken.issue(userId, codeHash, type,
            OffsetDateTime.now().plusMinutes(expirationMinutes));
    userTokenRepository.save(token);
    return code;
  }
}