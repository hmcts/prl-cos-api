package uk.gov.hmcts.reform.prl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
@Slf4j
public class FeignRetryConfig extends FeignClientProperties.FeignClientConfiguration {

    @Bean
    public feign.Retryer feignRetryer() {
        return new feign.Retryer.Default(500, 3000, 3);
    }

    @Bean
    public feign.codec.ErrorDecoder feignErrorDecoder() {
        return (methodKey, response) -> {
            log.info("Exception occured");
            // Only retry GET methods and 5xx responses
            boolean isGet = response.request().httpMethod() == feign.Request.HttpMethod.GET;
            int status = response.status();

            Collection<String> retryAfterHeader = response.headers().get("Retry-After");

            Long retryAfter = null;

            if (retryAfterHeader != null && !retryAfterHeader.isEmpty()) {
                retryAfter = Long.parseLong(retryAfterHeader.iterator().next()) * 1000;
            }
            log.info("http method {} and status {}",  response.request().httpMethod(), status);
            if (isGet && status >= 500 && status < 600) {
                log.info("retrying now for status {}",  status);
                return new feign.RetryableException(
                    status,
                    "Retryable 5xx for GET",
                    response.request().httpMethod(),
                    null,
                    retryAfter,
                    response.request()
                );
            }

            return feign.FeignException.errorStatus(methodKey, response);
        };
    }
}
