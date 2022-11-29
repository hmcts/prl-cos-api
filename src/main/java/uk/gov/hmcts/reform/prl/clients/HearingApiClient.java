package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.config.consts.CustomHttpHeaders;
import uk.gov.hmcts.reform.prl.models.hearings.HearingDetailsInfo;


@FeignClient(
    name = "fis-hmc-api",
    url = "${fis-hmc-api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface HearingApiClient {

    @GetMapping(value = "/hearings", consumes = "application/json")
    HearingDetailsInfo getHearingData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(CustomHttpHeaders.SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader("caseReference") String caseId
    );
}
