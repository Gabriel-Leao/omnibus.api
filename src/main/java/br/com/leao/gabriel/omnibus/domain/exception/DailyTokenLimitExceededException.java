package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Thrown when a user has requested more verification codes than allowed within 24 hours.
 */
public class DailyTokenLimitExceededException extends ConflictException {

  /**
   * Creates the exception with a generic message; does not reveal the configured limit.
   */
  public DailyTokenLimitExceededException() {
    super("Too many verification codes requested; try again later");
  }
}
