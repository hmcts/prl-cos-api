package uk.gov.hmcts.reform.prl.controllers.gatekeeping;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.gatekeeping.ListOnNoticeService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTE;
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

    @PostMapping(path = "/listOnNotice/reasonUpdation/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
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
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
