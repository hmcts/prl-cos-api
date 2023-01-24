package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ApplicantsListGenerator;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UploadAdditionalApplicationController {

    private final ApplicantsListGenerator applicantsListGenerator;

    private final ObjectMapper objectMapper;

    private final IdamClient idamClient;


    @PostMapping(path = "/pre-populate-applicants", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate applicants")
    public AboutToStartOrSubmitCallbackResponse prePopulateApplicants(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("additionalApplicantsList", applicantsListGenerator.buildApplicantsList(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/upload-additional-application/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create additional application bundle ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Bundle created"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse createUploadAdditionalApplicationBundle(@RequestHeader("Authorization")
                                                                                            @Parameter(hidden = true) String authorisation,
                                                                                        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("CaseData =====> " + caseData.getUplaodAdditionalApplicationData());
        //UserDetails userDetails = idamClient.getUserDetails(authorisation);
        //userDetails.getEmail();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        //WIP
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


}
