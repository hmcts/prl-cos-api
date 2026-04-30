package uk.gov.hmcts.reform.prl.clients;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogsRequest;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogsResponse;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridMessageResponse;

@FeignClient(
    name = "sendGridEmailLogsClient",
    url = "https://api.sendgrid.com",
    configuration = SendGridEmailLogsClient.FeignClientConfiguration.class
)
public interface SendGridEmailLogsClient {
    class FeignClientConfiguration {

        @Value("${send-grid.email-logs-api.api-key}")
        private String apiKey;

        @Bean
        public RequestInterceptor requestInterceptor() {
            return template -> template.header("Authorization", "Bearer " + apiKey);
        }
    }

    @PostMapping("/v3/logs")
    SendGridLogsResponse getLogs(@RequestBody SendGridLogsRequest request);

    @GetMapping("/v3/logs/{sgMessageId}")
    SendGridMessageResponse getMessage(@PathVariable String sgMessageId);
}
