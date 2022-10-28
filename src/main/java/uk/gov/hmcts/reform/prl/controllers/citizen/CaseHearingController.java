package uk.gov.hmcts.reform.prl.controllers.citizen;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.dto.citizen.hearing.HearingResponseData;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class CaseHearingController {


    @GetMapping(path = "/hearings/{caseId}", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public HearingResponseData getCaseHearings(
        @PathVariable("caseId") String caseId,
        @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String userToken,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {

        HearingResponseData hearingResponseData = HearingResponseData.builder()
            .date("Monday 12 July 2021")
            .time("10:30am")
            .durationOfHearing("2 hours")
            .typeOfHearing("Remote (by telephone or video)")
            .hearingLink("The court will email you instructions to join the hearing")
            .courtName("Bristol family court")
            .support("Support you need during your case")
            .hearingNotice("Check all details of the hearing in the hearing notice Hearing-notice-pdf")
            .build();

        return hearingResponseData;
    }

}
