package uk.gov.hmcts.reform.prl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.prl.tasks.ScheduledTaskRunner;


@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.prl.*", "uk.gov.hmcts.reform.prl.services", "uk.gov.hmcts.reform.idam.client",
                                    "uk.gov.hmcts.reform.prl.clients",
                                    "uk.gov.hmcts.reform.ccd.client","uk.gov.hmcts.reform.authorisation"
})
@EnableAsync
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = {  "uk.gov.hmcts.reform.prl", "uk.gov.hmcts.reform.prl.services",
    "uk.gov.hmcts.reform.prl.config","uk.gov.hmcts.reform.ccd.document","uk.gov.hmcts.reform.prl.repositories",
    "uk.gov.hmcts.reform.prl.mapper","uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.sendletter.api",
    "uk.gov.hmcts.reform.prl.clients",
    "uk.gov.hmcts.reform.prl.schedule",
    "uk.gov.hmcts.reform.prl.tasks"})
@Slf4j
public class Application implements CommandLineRunner {

    @Value("${runs-locally}")
    private boolean runsLocally;

    @Autowired
    private ScheduledTaskRunner scheduledTaskRunner;

    public static final String TASK_NAME = "TASK_NAME";

    public static void main(final String[] args) {
        final var application = new SpringApplication(Application.class);
        final var instance = application.run(args);

        if (System.getenv(TASK_NAME) != null) {
            instance.close();
        }
    }

    @PostConstruct
    public void initApp() {
        if (runsLocally) {
            log.info("Application running locally, turning off SSL verification so that tests accessing"
                         + " HTTPS resources can run on machines with ZScaler proxy");
        } else {
            log.info("Application not detected to run on a local machine");
        }
    }

    @Override
    public void run(String... args) {
        if (System.getenv(TASK_NAME) != null) {
            log.info("*** Running scheduled task: {} ", System.getenv(TASK_NAME));
            scheduledTaskRunner.run(System.getenv(TASK_NAME));
        }
    }
}
