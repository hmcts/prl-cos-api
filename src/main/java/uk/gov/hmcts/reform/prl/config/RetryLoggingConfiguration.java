package uk.gov.hmcts.reform.prl.config;

import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryLoggingConfiguration {
    private final RetryRegistry retryRegistry;

    @PostConstruct
    void registerRetryLogging() {
        retryRegistry.retry("searchCasesRetryConfig")
            .getEventPublisher()
            .onRetry(event -> log.warn(
                "Retrying '{}', attempt {}",
                event.getName(),
                event.getNumberOfRetryAttempts()
            ));
    }
}
