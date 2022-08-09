package uk.gov.hmcts.reform.prl.clients.rpe;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "package uk.gov.hmcts.reform.authorisation"
})
public class ServiceAuthConsumerApplication {
}
