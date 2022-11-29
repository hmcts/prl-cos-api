package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.hearings.HearingDetailsInfo;
import uk.gov.hmcts.reform.prl.services.HearingApiService;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
public class HearingApiController {

    @Autowired
    private HearingApiService hearingApiService;

    @GetMapping("/hearings/{caseId}")
    public HearingDetailsInfo getHearingInfo(@RequestHeader("Authorization") @Parameter(hidden = true)
                                                 String authorisation,
                                             @RequestParam("caseId") String caseId) throws Exception {
        return hearingApiService.getHearingApiResponse(authorisation,caseId);
    }

}
