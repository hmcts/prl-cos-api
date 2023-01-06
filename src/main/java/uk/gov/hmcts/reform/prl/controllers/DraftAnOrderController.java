package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_EX740;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_QUALIFIED_LEGAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_NOT_NEEDED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JOINING_INSTRUCTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARENT_WITHCARE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTICIPATION_DIRECTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RIGHT_TO_ASK_COURT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SAFE_GUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPECIFIED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPIP_ATTENDANCE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UPDATE_CONTACT_DETAILS;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DraftAnOrderController {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ManageOrderService manageOrderService;

    @Autowired
    private DraftAnOrderService draftAnOrderService;

    private final LocationRefDataService locationRefDataService;

    @PostMapping(path = "/reset-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to reset fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to reset fields"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse resetFields(
        @RequestBody CallbackRequest callbackRequest) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(Collections.emptyMap()).build();
    }

    @PostMapping(path = "/selected-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateHeader(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder()
                      .selectedOrder(caseData.getCreateSelectOrderOptions() != null
                                         ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : "")
                      .build().toMap(CcdObjectMapper.getObjectMapper())).build();

    }

    @PostMapping(path = "/populate-draft-order-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateFl404Fields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("caseTypeOfApplication", caseData.getCaseTypeOfApplication());

        if (!(CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(caseData.getCreateSelectOrderOptions())
            || CreateSelectOrderOptionsEnum.blankOrderOrDirectionsWithdraw.equals(caseData.getCreateSelectOrderOptions()))
            && PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
        ) {
            log.info("Court name before prepopulate: {}", caseData.getCourtName());
            caseData = manageOrderService.populateCustomOrderFields(caseData);
        } else {
            caseData = draftAnOrderService.generateDocument(callbackRequest, caseData);
            caseDataUpdated.putAll(manageOrderService.getCaseData(authorisation, caseData));
        }
        caseDataUpdated.putAll(caseData.toMap(CcdObjectMapper.getObjectMapper()));
        log.info("Case data updated map {}", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated).build();
    }

    @PostMapping(path = "/populate-standard-direction-order-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate standard direction order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateSdoFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getStandardDirectionOrder().getSdoPreamblesList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoLocalAuthorityList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoOtherList().isEmpty()) {
            List<String> errorList = new ArrayList<>();
            errorList.add(
                "Please select at least one options from below");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errorList)
                .build();
        } else {
            if (!caseData.getStandardDirectionOrder().getSdoPreamblesList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoPreamblesList().contains(SdoPreamblesEnum.rightToAskCourt)) {
                caseDataUpdated.put(
                    "sdoRightToAskCourt",
                    RIGHT_TO_ASK_COURT
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping)) {
                caseDataUpdated.put(
                    "sdoNextStepsAfterSecondGK",
                    SAFE_GUARDING_LETTER
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.hearingNotNeeded)) {
                caseDataUpdated.put(
                    "sdoHearingNotNeeded",
                    HEARING_NOT_NEEDED
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.hearingNotNeeded)) {
                caseDataUpdated.put(
                    "sdoParticipationDirections",
                    PARTICIPATION_DIRECTIONS
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.joiningInstructions)) {
                caseDataUpdated.put(
                    "sdoJoiningInstructionsForRH",
                    JOINING_INSTRUCTIONS
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.updateContactDetails)) {
                caseDataUpdated.put(
                    "sdoUpdateContactDetails",
                    UPDATE_CONTACT_DETAILS
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoCourtList().contains(
                SdoCourtEnum.crossExaminationEx740)) {
                caseDataUpdated.put(
                    "sdoCrossExaminationEx740",
                    CROSS_EXAMINATION_EX740
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoCourtList().contains(
                SdoCourtEnum.crossExaminationQualifiedLegal)) {
                caseDataUpdated.put(
                    "sdoCrossExaminationQualifiedLegal",
                    CROSS_EXAMINATION_QUALIFIED_LEGAL
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().contains(
                SdoDocumentationAndEvidenceEnum.specifiedDocuments)) {
                caseDataUpdated.put(
                    "sdoSpecifiedDocuments",
                    SPECIFIED_DOCUMENTS
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().contains(
                SdoDocumentationAndEvidenceEnum.spipAttendance)) {
                caseDataUpdated.put(
                    "sdoSpipAttendance",
                    SPIP_ATTENDANCE
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoOtherList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoOtherList().contains(
                SdoOtherEnum.parentWithCare)) {
                caseDataUpdated.put(
                    "sdoParentWithCare",
                    PARENT_WITHCARE
                );
            }
            populateCourtDynamicList(authorisation, caseDataUpdated);

        }
        log.info("Case data updated map {}", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated).build();
    }

    private void populateCourtDynamicList(String authorisation, Map<String, Object> caseDataUpdated) {
        List<DynamicListElement> courtList = locationRefDataService.getCourtLocations(authorisation);
        DynamicList courtDynamicList =  DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build();
        caseDataUpdated.put(
            "sdoUrgentHearingCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoFhdraCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoDirectionsDraCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoSettlementConferenceCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoTransferApplicationCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "sdoCrossExaminationCourtDynamicList", courtDynamicList);
    }

    @PostMapping(path = "/generate-doc", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse generateDoc(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData = draftAnOrderService.generateDocument(callbackRequest, caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getCreateSelectOrderOptions() != null
            && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
            List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
            manageOrderService.updateCaseDataWithAppointedGuardianNames(callbackRequest.getCaseDetails(), namesList);
            caseData.setAppointedGuardianName(namesList);
        }
        caseDataUpdated.putAll(manageOrderService.getCaseData(authorisation, caseData));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate draft order collection")
    public AboutToStartOrSubmitCallbackResponse prepareDraftOrderCollection(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(draftAnOrderService.generateDraftOrderCollection(caseData));
        log.info("*** before returning {} ***", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


}

