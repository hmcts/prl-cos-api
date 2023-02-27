package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

@FeignClient(
    name = "hearing-api",
    url = "${fis_hearing.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface HearingApiClient {

    @GetMapping(path = "/hearings")
    Hearings getHearingDetails(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference
    );

    @GetMapping(path = "/getNextHearingDate")
    NextHearingDetails getNextHearingDate(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference
    );

}
