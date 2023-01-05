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
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfirmRecipients;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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


    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<DynamicMultiselectListElement> listElements = new ArrayList<>();
        if (caseData.getOrderCollection() != null && !caseData.getOrderCollection().isEmpty()) {
            caseData.getOrderCollection().forEach(order ->
                listElements.add(DynamicMultiselectListElement.builder()
                                     .code(order.getValue().getOrderTypeId())
                                     .label(order.getValue().getLabelForDynamicList())
                                     .build())
            );
            caseDataUpdated.put("serviceOfApplicationScreen1", DynamicMultiSelectList
                .builder().listItems(listElements).build());

            log.info("***** listElements : {}", caseDataUpdated.get("serviceOfApplicationScreen1"));
            log.info("***** listElements : {}", caseDataUpdated);
        }
        List<DynamicMultiselectListElement> applicantList = new ArrayList<>();
        List<DynamicMultiselectListElement> applicantSolicitorList = new ArrayList<>();
        List<DynamicMultiselectListElement> respondentList = new ArrayList<>();
        List<DynamicMultiselectListElement> respondentSolicitorList = new ArrayList<>();
        List<DynamicMultiselectListElement> otherPeopleList = new ArrayList<>();
        if (caseData != null && caseData.getCaseTypeOfApplication().equalsIgnoreCase("C100")) {
            caseData.getApplicants().forEach(applicant -> {
                applicantList.add(DynamicMultiselectListElement.builder()
                                                                      .code(applicant.getId().toString())
                                                                      .label(applicant.getValue().getFirstName() + " "
                                                                                 + applicant.getValue().getLastName())
                                                                      .build());

                applicantSolicitorList.add(DynamicMultiselectListElement.builder()
                                           .code(applicant.getId().toString())
                                           .label(applicant.getValue().getRepresentativeFirstName() + " "
                                           + applicant.getValue().getRepresentativeLastName())
                                           .build());


            });
            log.info("****** applicantList : {}", applicantList);
            log.info("****** applicantSolicitorList : {}", applicantSolicitorList);
            caseData.getRespondents().forEach(respondent -> {

                respondentList.add(DynamicMultiselectListElement.builder()
                                       .code(respondent.getId().toString())
                                       .label(respondent.getValue().getFirstName() + " "
                                                  + respondent.getValue().getLastName())
                                       .build());
                if (YesNoDontKnow.yes.equals(respondent.getValue().getDoTheyHaveLegalRepresentation())) {
                    respondentSolicitorList.add(DynamicMultiselectListElement.builder()
                                                    .code(respondent.getId().toString())
                                                    .label(respondent.getValue().getRepresentativeFirstName() + " "
                                                               + respondent.getValue().getRepresentativeLastName())
                                                    .build());
                }
            });
            if (caseData.getOthersToNotify() != null) {
                caseData.getOthersToNotify().forEach(others ->
                    otherPeopleList.add(DynamicMultiselectListElement.builder()
                                            .code(others.getId().toString())
                                            .label(others.getValue().getFirstName() + " " + others.getValue().getLastName())
                                            .build())
                );
            }
            log.info("****** respondent list : {}", respondentList);
            log.info("****** respondentSolicitorList : {}", respondentSolicitorList);
        }
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
            .build();
        caseDataUpdated.put("confirmRecipients",confirmRecipients);
        log.info("****** confirm recepietns : {}", confirmRecipients);
        caseDataUpdated.put("sentDocumentPlaceHolder", serviceOfApplicationService.getCollapsableOfSentDocuments());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Serve Parties Email Notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = serviceOfApplicationService.sendEmail(callbackRequest.getCaseDetails());
        serviceOfApplicationService.sendPost(callbackRequest.getCaseDetails(), authorisation);
        Map<String,Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        log.info("** submit list items : {}", updatedCaseData.get("serviceOfApplicationScreen1"));
        updatedCaseData.put("caseInvites", caseData.getCaseInvites());
        Map<String, Object> allTabsFields = allTabService.getAllTabsFields(caseData);
        updatedCaseData.putAll(allTabsFields);
        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCaseData).build();
    }
}
