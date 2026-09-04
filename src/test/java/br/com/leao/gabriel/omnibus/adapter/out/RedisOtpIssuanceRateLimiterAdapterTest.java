package br.com.leao.gabriel.omnibus.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisOtpIssuanceRateLimiterAdapterTest {

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @Test
  void shouldSetExpirationWhenCounterIsCreated() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment("otp-rate-limit:user-id:ACCOUNT_ACTIVATION")).thenReturn(1L);
    var adapter = new RedisOtpIssuanceRateLimiterAdapter(redisTemplate);

    long count = adapter.incrementAndGet("user-id", OtpType.ACCOUNT_ACTIVATION);

    assertThat(count).isOne();
    verify(redisTemplate).expire("otp-rate-limit:user-id:ACCOUNT_ACTIVATION", Duration.ofHours(24));
  }

  @Test
  void shouldNotSetExpirationForExistingCounter() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment("otp-rate-limit:user-id:PASSWORD_RESET")).thenReturn(2L);
    var adapter = new RedisOtpIssuanceRateLimiterAdapter(redisTemplate);

    assertThat(adapter.incrementAndGet("user-id", OtpType.PASSWORD_RESET)).isEqualTo(2);
  }
}
