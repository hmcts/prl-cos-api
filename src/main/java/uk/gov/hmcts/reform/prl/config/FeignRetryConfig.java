package uk.gov.hmcts.reform.prl.config;

import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
public class FeignRetryConfig extends FeignClientProperties.FeignClientConfiguration {

    @Bean
    public feign.Retryer feignRetryer() {
        return new feign.Retryer.Default(500, 3000, 3);
    }

    @Bean
    public feign.codec.ErrorDecoder feignErrorDecoder() {
        return (methodKey, response) -> {
            // Only retry GET methods and 5xx responses
            boolean isGet = response.request().httpMethod() == feign.Request.HttpMethod.GET;
            int status = response.status();

            Collection<String> retryAfterHeader = response.headers().get("Retry-After");

            Long retryAfter = null;

            if (retryAfterHeader != null && !retryAfterHeader.isEmpty()) {
                retryAfter = Long.parseLong(retryAfterHeader.iterator().next()) * 1000;
            }

            if (isGet && status >= 500 && status < 600) {
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
