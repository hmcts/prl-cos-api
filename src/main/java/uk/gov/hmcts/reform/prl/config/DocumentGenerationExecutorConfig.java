package uk.gov.hmcts.reform.prl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class DocumentGenerationExecutorConfig {

    public static final String RESUBMISSION_DOCUMENT_EXECUTOR = "resubmissionDocumentExecutor";

    @Bean(name = RESUBMISSION_DOCUMENT_EXECUTOR)
    public Executor resubmissionDocumentExecutor(
        @Value("${document-generation.resubmission-concurrency:4}") int concurrency
    ) {
        if (concurrency < 1) {
            throw new IllegalArgumentException("document-generation.resubmission-concurrency must be at least 1");
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(concurrency);
        executor.setMaxPoolSize(concurrency);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("resubmission-document-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
