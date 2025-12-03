package uk.gov.hmcts.reform.prl.clients.os;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;

@FeignClient(name = "os-court-finder-api", primary = false, url = "${postcodelookup.api.url}",
    configuration = OsCourtFinderApi.FeignClientConfiguration.class)
public interface OsCourtFinderApi {

    class FeignClientConfiguration {

        @Value("${postcodelookup.api.key}")
        private String apiKey;

        @Bean
        public RequestInterceptor osPlacesRequestInterceptor() {

            return new RequestInterceptor() {
                @Override
                public void apply(RequestTemplate template) {
                    template.query("key", apiKey);
                    template.query("maxresults", "1");
                }
            };
        }
    }

    @GetMapping(value = "/postcode", consumes = "application/json")
    OsPlacesResponse findCouncilByPostcode(@RequestParam("postcode") String postcode);

}
