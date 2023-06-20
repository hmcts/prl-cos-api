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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfirmRecipients;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
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
    private AuthorisationService authorisationService;

    @Autowired
    WelshCourtEmail welshCourtEmail;

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            List<DynamicMultiselectListElement> listElements = new ArrayList<>();
            caseDataUpdated.put(
                "serviceOfApplicationScreen1",
                dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData, null)
            );

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
            ConfirmRecipients confirmRecipients = ConfirmRecipients.builder()
                .applicantsList(DynamicMultiSelectList.builder()
                                    .listItems(applicantList)
                                    .build())
                .applicantSolicitorList(DynamicMultiSelectList.builder()
                                            .listItems(applicantSolicitorList)
                                            .build())
                .respondentsList(DynamicMultiSelectList.builder()
                                     .listItems(respondentList)
                                     .build())
                .respondentSolicitorList(DynamicMultiSelectList.builder()
                                             .listItems(respondentSolicitorList)
                                             .build())
                .otherPeopleList(DynamicMultiSelectList.builder()
                                     .listItems(otherPeopleList)
                                     .build())
                .cafcassEmailAddressList(cafcassCymruEmailAddress != null ? List.of(element(cafcassCymruEmailAddress)) : null)
                .build();
            caseDataUpdated.put("confirmRecipients", confirmRecipients);
            caseDataUpdated.put("sentDocumentPlaceHolder", serviceOfApplicationService.getCollapsableOfSentDocuments());
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Serve Parties Email Notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = serviceOfApplicationService.sendEmail(callbackRequest.getCaseDetails());
            //serviceOfApplicationService.sendPost(callbackRequest.getCaseDetails(), authorisation);
            Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
            updatedCaseData.put("caseInvites", caseData.getCaseInvites());
            Map<String, Object> allTabsFields = allTabService.getAllTabsFields(caseData);
            updatedCaseData.putAll(allTabsFields);
            return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCaseData).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
