package uk.gov.hmcts.reform.prl.clients.bundle;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.prl.clients"
})
public class BundleApiConsumerApplication {
}
