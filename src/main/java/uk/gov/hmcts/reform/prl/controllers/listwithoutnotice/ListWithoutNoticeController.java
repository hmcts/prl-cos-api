package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTWITHOUTNOTICE_HEARINGDETAILS;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ListWithoutNoticeController extends AbstractCallbackController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    HearingDataService hearingDataService;

    @Autowired
    LocationRefDataService locationRefDataService;

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    AllocatedJudgeService allocatedJudgeService;

    @PostMapping(path = "/pre-populate-hearingPage-Data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate Hearing page details")
    public AboutToStartOrSubmitCallbackResponse prePopulateHearingPageData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
        log.info("Inside Prepopulate prePopulateHearingPageData for the case id {}", caseReferenceNumber);
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        List<Element<HearingData>> existingListWithoutNoticeHearingDetails = caseData.getListWithoutNoticeHearingDetails();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseDataUpdated.containsKey(LISTWITHOUTNOTICE_HEARINGDETAILS)) {
            caseDataUpdated.put(
                LISTWITHOUTNOTICE_HEARINGDETAILS,
                hearingDataService.getHearingData(existingListWithoutNoticeHearingDetails,hearingDataPrePopulatedDynamicLists));
        } else {
            caseDataUpdated.put(
                LISTWITHOUTNOTICE_HEARINGDETAILS,
                ElementUtils.wrapElements(hearingDataService.generateHearingData(hearingDataPrePopulatedDynamicLists,caseData.getApplicantName())));

        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/listWithoutNotice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List Without Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List Without notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse listWithoutNoticeSubmission(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Without Notice Submission flow - case id : {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("****listWithoutNoticeSubmission***** {} ", callbackRequest.toString());
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        caseDataUpdated.put(LISTWITHOUTNOTICE_HEARINGDETAILS, hearingDataService
            .getHearingData(caseData.getListWithoutNoticeHearingDetails(),null));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
