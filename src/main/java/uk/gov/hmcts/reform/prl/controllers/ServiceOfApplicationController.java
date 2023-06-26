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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
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
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
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
    private LaunchDarklyClient launchDarklyClient;

    @Autowired
    CoreCaseDataService coreCaseDataService;

    private Map<String, Object> caseDataUpdated;

    @Autowired
    WelshCourtEmail welshCourtEmail;

    public static final String CONFIRMATION_HEADER = "# The application is served";
    public static final String CONFIRMATION_BODY_PREFIX = "### What happens next \n\n The document packs will be served to parties ";

    public static final String CONFIDENTIAL_CONFIRMATION_HEADER = "# The application will be reviewed for confidential details";
    public static final String CONFIDENTIAL_CONFIRMATION_BODY_PREFIX = "### What happens next \n\n The document will "
        + "be reviewed for confidential details";

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(serviceOfApplicationService.getSoaCaseFieldsMap(
            callbackRequest.getCaseDetails())).build();
    }


    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Serve Parties Email and Post Notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (caseData.getServiceOfApplication() != null && caseData.getServiceOfApplication().getProceedToServing() != null && YesOrNo.No.equals(
            caseData.getServiceOfApplication().getProceedToServing())) {
            log.info("Confidential details are present, case needs to be reviewed and served later");
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIDENTIAL_CONFIRMATION_HEADER).confirmationBody(
                CONFIDENTIAL_CONFIRMATION_BODY_PREFIX).build());
        }
        log.info("Confidential details are NOT present");
        log.info("inside submitted--start of notification");
        if (caseData.getServiceOfApplication() != null && SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            caseData.getApplicants().get(0).getValue().getResponse().getCitizenFlags().setIsApplicationServed(YesOrNo.Yes);
        };
        if (caseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            log.info("*** finalServedApplicationDetailsList is empty in case data ***");
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorisation
        )));
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        caseDataMap.put("finalServedApplicationDetailsList", finalServedApplicationDetailsList);
        if (launchDarklyClient.isFeatureEnabled("soa-access-code-gov-notify")) {
            caseDataMap.put("caseInvites", serviceOfApplicationService.sendAndReturnCaseInvites(caseData));
        }
        serviceOfApplicationService.cleanUpSoaSelections(caseDataMap);
        log.info("After {}", caseDataMap);
        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-all-tabs",
            caseDataMap
        );
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            CONFIRMATION_HEADER).confirmationBody(
            CONFIRMATION_BODY_PREFIX).build());
    }
}
