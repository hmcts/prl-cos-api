package uk.gov.hmcts.reform.prl.controllers.gatekeeping;


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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.ListOnNoticeService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_REASONS_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REASONS_SELECTED_FOR_LIST_ON_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBJECT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ListOnNoticeController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ListOnNoticeService listOnNoticeService;

    @Autowired
    private RefDataUserService refDataUserService;

    @Autowired
    private CoreCaseDataService coreCaseDataService;

    @PostMapping(path = "/listOnNotice/reasonUpdation/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = " mid-event for updating the reason")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse listOnNoticeMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("*** mid event triggered for List ON Notice : {}", caseData.getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String reasonsSelectedForListOnNotice =
            listOnNoticeService.getReasonsSelected(caseDataUpdated.get(LIST_ON_NOTICE_REASONS_SELECTED),caseData.getId());
        if (null != reasonsSelectedForListOnNotice && reasonsSelectedForListOnNotice != "") {
            caseDataUpdated.put(SELECTED_AND_ADDITIONAL_REASONS,reasonsSelectedForListOnNotice);
            caseDataUpdated.put(CASE_NOTE, caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS));
            caseDataUpdated.put(SUBJECT,REASONS_SELECTED_FOR_LIST_ON_NOTICE);
        }
        log.info("*** value of  SELECTED_AND_ADDITIONAL_REASONS : {}", caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS));
        log.info("*** value of  CASE_NOTE : {}", caseDataUpdated.get(CASE_NOTE));
        log.info("*** value of  SUBJECT : {}", caseDataUpdated.get(SUBJECT));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/listOnNotice/additionalReasons/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = " mid-event to update the additional reason")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse listOnNoticeAdditionalReasonsMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("*** mid event triggered for List ON Notice to update the additional reasons : {}", caseData.getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(CASE_NOTE, null != caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS)
            ? (String)caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS) : null);
        caseDataUpdated.put(SUBJECT,REASONS_SELECTED_FOR_LIST_ON_NOTICE);
        log.info("*** value of  SELECTED_AND_ADDITIONAL_REASONS in second midevent: {}", caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS));
        log.info("*** value of  CASE_NOTE : {}", caseDataUpdated.get(CASE_NOTE));
        log.info("*** value of  SUBJECT : {}", caseDataUpdated.get(SUBJECT));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }



    @PostMapping(path = "/pre-populate-list-on-notice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate list on notice")
    public AboutToStartOrSubmitCallbackResponse prePopulateListOnNotice(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
        log.info("Inside Prepopulate prePopulate for the case id {}", caseReferenceNumber);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/listOnNotice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List On Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List ON notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse listOnNoticeSubmission(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Long id = callbackRequest.getCaseDetails().getId();
        log.info("List on Notice Submission flow - case id : {}", id);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("*** value of  CASE_NOTE in about to submit event : {}", caseDataUpdated.get(CASE_NOTE));
        log.info("*** value of  SUBJECT : {}", caseDataUpdated.get(SUBJECT));
        if (null != caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS)) {
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                id,
                "addCaseNote",
                Map.of(
                    CASE_NOTE,
                    (String)caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS),
                    SUBJECT,
                    REASONS_SELECTED_FOR_LIST_ON_NOTICE,
                    "id",
                    String.valueOf(id)
                )
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

}
