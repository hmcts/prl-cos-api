package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RestController
@RequestMapping("/service-of-application")
@Slf4j
public class ServiceOfApplicationController {

    @Autowired
    private ServiceOfApplicationService serviceOfApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    AllTabServiceImpl allTabService;

    @Autowired
    DynamicMultiSelectListService dynamicMultiSelectListService;

    @Autowired
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Autowired
    CoreCaseDataService coreCaseDataService;

    private Map<String, Object> caseDataUpdated;

    @Autowired
    WelshCourtEmail welshCourtEmail;

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, List<DynamicMultiselectListElement>> applicantDetails = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData);
        List<DynamicMultiselectListElement> applicantList = applicantDetails.get("applicants");
        List<DynamicMultiselectListElement> applicantSolicitorList = applicantDetails.get("applicantSolicitors");
        Map<String, List<DynamicMultiselectListElement>> respondentDetails = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData);
        List<DynamicMultiselectListElement> respondentList = respondentDetails.get("respondents");
        List<DynamicMultiselectListElement> respondentSolicitorList = respondentDetails.get("respondentSolicitors");
        List<DynamicMultiselectListElement> otherPeopleList = dynamicMultiSelectListService.getOtherPeopleMultiSelectList(
            caseData);


        String cafcassCymruEmailAddress = welshCourtEmail
            .populateCafcassCymruEmailInManageOrders(caseData);
        caseDataUpdated.put("soaRecipientsOptions", serviceOfApplicationService.getCombinedRecipients(caseData));
        caseDataUpdated.put("soaOtherParties", DynamicMultiSelectList.builder()
            .listItems(otherPeopleList)
            .build());
        caseDataUpdated.put("soaOtherPeoplePresentInCaseFlag", otherPeopleList.size() > 0 ? YesOrNo.Yes : YesOrNo.No);
        caseDataUpdated.put("soaCafcassEmailAddressList", cafcassCymruEmailAddress != null
            ? List.of(element(cafcassCymruEmailAddress)) : null);
        caseDataUpdated.put(
            "serviceOfApplicationScreen1",
            dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData, null)
        );
        caseDataUpdated.put(
            "isCafcass",
            serviceOfApplicationService.getCafcass(caseData)
        );
        caseDataUpdated.put("sentDocumentPlaceHolder", serviceOfApplicationService.getCollapsableOfSentDocuments());
        caseDataUpdated.put("caseTypeOfApplication", CaseUtils.getCaseTypeOfApplication(caseData));
        log.info("Updated casedata {}", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    /*@PostMapping(path = "/mid", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Serve Parties Email Notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("Confirm recipients in mid before {}", caseData.getServiceOfApplication());
        Map<String, List<DynamicMultiselectListElement>> applicantDetails = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData);
        List<DynamicMultiselectListElement> applicantList = applicantDetails.get("applicants");
        List<DynamicMultiselectListElement> applicantSolicitorList = applicantDetails.get("applicantSolicitors");
        Map<String, List<DynamicMultiselectListElement>> respondentDetails = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData);
        List<DynamicMultiselectListElement> respondentList = respondentDetails.get("respondents");
        List<DynamicMultiselectListElement> respondentSolicitorList = respondentDetails.get("respondentSolicitors");
        List<DynamicMultiselectListElement> otherPeopleList = dynamicMultiSelectListService.getOtherPeopleMultiSelectList(
            caseData);
        String cafcassCymruEmailAddress = welshCourtEmail
            .populateCafcassCymruEmailInManageOrders(caseData);

        ServiceOfApplication confirmRecipients = ServiceOfApplication.builder()
            .soaApplicantsList(DynamicMultiSelectList.builder()
                                   .listItems(applicantList)
                                   .build())
            .soaRespondentsList(DynamicMultiSelectList.builder()
                                    .listItems(respondentList)
                                    .build())
            .soaOtherPeopleList(DynamicMultiSelectList.builder()
                                    .listItems(otherPeopleList)
                                    .build())
            .soaCafcassEmailAddressList(cafcassCymruEmailAddress != null ? List.of(element(cafcassCymruEmailAddress)) : null)
            .build();
        caseData = caseData.toBuilder().serviceOfApplication(confirmRecipients).build();
        log.info("Confirm recipients in mid after {}", caseData.getServiceOfApplication());
        caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(caseData.toMap(CcdObjectMapper.getObjectMapper()));
        log.info("Confirm recipients in mid from map {}", caseDataUpdated.get("confirmRecipients"));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }*/

    /*@PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Serve Parties Email Notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        log.info("inside about to submit");
        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCaseData).build();
    }*/

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Serve Parties Email and Post Notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public void handleSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("casetype in /submitted" + caseData.getCaseTypeOfApplication());
        log.info("inside submitted--start of notification");
        log.info("Confirm recipients {}", caseData.getFinalServedApplicationDetailsList());
        if (caseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            log.info("*** finalServedApplicationDetailsList is empty in case data ***");
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(serviceOfApplicationService.sendNotificationForServiceOfApplication(
            callbackRequest.getCaseDetails(),
            authorisation
        )));
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("finalServedApplicationDetailsList", finalServedApplicationDetailsList);
        log.info("finalServedApplicationDetailsList {}", finalServedApplicationDetailsList);
        log.info("caseDataMap {}", caseDataMap);
        serviceOfApplicationService.cleanUpSoaSelections(caseDataMap);
        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-all-tabs",
            caseDataMap
        );
        log.info("finalServedApplicationDetailsList" + caseDataMap.get("finalServedApplicationDetailsList"));
        log.info("inside submitted--end of notification");
    }
}
