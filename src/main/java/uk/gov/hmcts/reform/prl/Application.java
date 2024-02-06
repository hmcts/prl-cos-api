package uk.gov.hmcts.reform.prl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.prl.*", "uk.gov.hmcts.reform.prl.services", "uk.gov.hmcts.reform.idam.client",
                                    "uk.gov.hmcts.reform.prl.clients",
                                    "uk.gov.hmcts.reform.ccd.client","uk.gov.hmcts.reform.authorisation"
})
/*
 I don't know why this was not working, but I did what was suggested here:
 https://stackoverflow.com/questions/26889970/
 intellij-incorrectly-saying-no-beans-of-type-found-for-autowired-repository/41766552
    @SpringBootApplication(
    scanBasePackages = {
        "uk.gov.hmcts.reform.prl"
      }
    )
 */
@EnableAsync
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {  "uk.gov.hmcts.reform.prl", "uk.gov.hmcts.reform.prl.services",
    "uk.gov.hmcts.reform.prl.config","uk.gov.hmcts.reform.ccd.document","uk.gov.hmcts.reform.prl.repositories",
    "uk.gov.hmcts.reform.prl.mapper","uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.sendletter.api",
    "uk.gov.hmcts.reform.prl.clients"})
@Slf4j
public class Application {

    @Value("${runs-locally}")
    private boolean runsLocally;

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void initApp() throws Exception {
        if (runsLocally) {
            log.info("Application running locally, turning off SSL verification so that tests accessing"
                         + " HTTPS resources can run on machines with ZScaler proxy");
        } else {
            log.info("Application not detected to run on a local machine");
        }
    }
}
