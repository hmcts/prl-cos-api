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
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
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
                    "As the direction has been made without hearing may ask the court to reconsider this order. "
                        + "You must do that within seven days of receiving the order by writing to the court"
                        + "(and notifying any other party) and asking the court to reconsider. "
                        + "Alternatively, the court may reconsider the directions at the first hearing"
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping)) {
                caseDataUpdated.put(
                    "sdoNextStepsAfterSecondGK",
                    "The court has considered the safeguarding letter from Cafcass or Cafcass Cymru "
                        + "and made a decision on how to progress your case."
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.hearingNotNeeded)) {
                caseDataUpdated.put(
                    "sdoHearingNotNeeded",
                    "A[Judge/justices' legal adviser] has decided that appropriate directions "
                        + "can be given to progress the matter without the need for a hearing"
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.hearingNotNeeded)) {
                caseDataUpdated.put(
                    "sdoParticipationDirections",
                    "If they not already done so, any part who considers that specific "
                        + "measures need to be taken to enable a party or witness to understand the"
                        + "proceedings and their role in them when in court, put their views to the  "
                        + "court, instruct their representatives before, during, and after the hearing "
                        + "or attend the hearing without significant distress should file an application "
                        + "notice and include the following information as far as practicable:"
                        + System.lineSeparator()
                        + "a. why the party or witness would benefit from assistance;"
                        + System.lineSeparator()
                        + "b. the measure or measures that would be likely to maximise as fas as practicable the "
                        + "quality of their evidence or participation and why;"
                        + System.lineSeparator()
                        + "c.written confirmations from any relevant witness of his/her views."
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.joiningInstructions)) {
                caseDataUpdated.put(
                    "sdoJoiningInstructionsForRH",
                    "Joining instructions"
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(
                SdoHearingsAndNextStepsEnum.updateContactDetails)) {
                caseDataUpdated.put(
                    "sdoUpdateContactDetailsSelection",
                    "The parties must, if their contact details have changed or missing from "
                        + "the applications, contact Cafcass or Cafcass Cymru quoting the case "
                        + "number at [CafcassCymruCAT@gov.wales/ privatelawapplications@cafcass.gov.uk]"
                        + "The email must include telephone contact details and email address so that they "
                        + "may be contacted for safeguarding purposes."
                        + System.lineSeparator()
                        + "Alternatively if any party is managing their case using the online dashboard, "
                        + "they can update their contact details on the and donot have to also contact "
                        + "Cafcass or Cafcass Cymru."
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoCourtList().contains(
                SdoCourtEnum.crossExaminationEx740)) {
                caseDataUpdated.put(
                    "sdoCrossExaminationEx740Section",
                    "Under Section 31U of the MFPA, it appears to the court that the quality of "
                        + "the party's evidence on cross-examination is likely to be diminshed if the "
                        + "cross examination is conducted in person, or if the conduct of cross-examination "
                        + "in person would cause significant distress to a party and it would not be "
                        + "contrary of justice to make the direction."
                        + System.lineSeparator()
                        + "It is ordered that:"
                        + System.lineSeparator()
                        + "a. The applicant and respondent(delete as appropriate) must notify the court by "
                        + "4pm on[date] whether they intend to appoint their own qualified legal representative."
                        + System.lineSeparator()
                        + "b. If the applicant/respondent does not intend to appoint their own qulaified leagl "
                        + "representative, they (whichever party is the (alleged) victim of domestic abuse) "
                        + "must complete form EX740 (name the form) and return it to the court by 4pm on [date] "
                );
            }
            if (!caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
                && caseData.getStandardDirectionOrder().getSdoCourtList().contains(
                SdoCourtEnum.crossExaminationQualifiedLegal)) {
                caseDataUpdated.put(
                    "sdoCrossExaminationQualifiedLegalSection",
                    "Should a qualified legal representative be appointed by the court "
                        + System.lineSeparator()
                        + "The court has considered whether it necessary in the interest of justice for the "
                        + "witness(es) to be cross-examined by a qualified legal representative(s) and concluded "
                        + "that it is neccessary to appoint such a qualified legal representative(s) to conduct "
                        + "the cross examination."
                        + System.lineSeparator()
                        + "1. The court is to appoint a qualified leagal representaive on behalf of [name/s] "
                        + "for the hearing listed on [date] at [time] at [name of court]."
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

