package uk.gov.hmcts.reform.prl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class DocumentGenerationExecutorVirtualConfig {

    public static final String DOCUMENT_EXECUTOR_FACTORY = "documentVirtualThreadFactory";
    public static final String DOCUMENT_EXECUTOR_SERVICE = "documentVirtualExecutorService";

    /**
     * Method that returns a factory of virtual threads with a custom name for the virtual threads
     * Keeps it generic to be used for any document generation tasks
     * @return ThreadFactory
     */
    @Bean(name = DOCUMENT_EXECUTOR_FACTORY)
    public ThreadFactory documentVirtualThreadFactory () {
        return Thread.ofVirtual().name("DocumentExecutor_", 1).factory();
    }

    /**
     * Method that returns an Executor Service that will create one virtual thread per task submitted to it
     * @param documentVirtualThreadFactory the factory of virtual threads configured
     * @return ExecutorService
     */
    @Bean(name = DOCUMENT_EXECUTOR_SERVICE, destroyMethod = "close")
    public ExecutorService documentVirtualExecutorService(
        ThreadFactory documentVirtualThreadFactory
    ) {
        return Executors.newThreadPerTaskExecutor(documentVirtualThreadFactory);
    }

}
