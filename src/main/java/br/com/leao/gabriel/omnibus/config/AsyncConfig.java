package br.com.leao.gabriel.omnibus.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Enables asynchronous method execution and provides the thread pool used for it.
 *
 * <p>This exists primarily so that email delivery (see {@code SmtpOtpSenderAdapter}) never runs
 * on the HTTP request thread. Several endpoints are deliberately designed to return the same
 * response regardless of whether an email is registered, to avoid leaking account existence (e.g.
 * {@code POST /password-reset}, {@code POST /auth/resend-activation}). If the email were sent
 * synchronously, the branch that actually sends an email would take measurably longer than the
 * branch that returns early — an attacker could infer whether an email exists purely from response
 * time, even though the response body is identical either way. Running the send asynchronously
 * removes that timing signal: the HTTP response no longer waits on it.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("notification-");
    executor.initialize();
    return executor;
  }
}
