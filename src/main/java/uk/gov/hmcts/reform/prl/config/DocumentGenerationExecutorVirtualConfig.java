package uk.gov.hmcts.reform.prl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

@Configuration
public class DocumentGenerationExecutorVirtualConfig {

    public static final String DOCUMENT_EXECUTOR = "documentExecutor";

    /**
     * Method that returns a factory of virtual threads with a custom name for the virtual threads
     * Keeps it generic to be used for any document generation tasks
     * @return ThreadFactory
     */
    @Bean(name = DOCUMENT_EXECUTOR)
    public ThreadFactory documentExecutor () {
        return Thread.ofVirtual().name("DocumentExecutor_", 1).factory();
    }
}
