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
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RestController
@RequestMapping("/confidentiality-check")
@Slf4j
public class ConfidentialityCheckController {

    @Autowired
    private ServiceOfApplicationService serviceOfApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    CoreCaseDataService coreCaseDataService;

    public static final String RETURNED_TO_ADMIN_HEADER = "# Application returned to admin";
    public static final String APPLICATION_SERVED_HEADER = "# Application served";

    public static final String CONFIDENTIAL_CONFIRMATION_NO_HEADER = "# The application will be served";
    public static final String CONFIDENTIAL_CONFIRMATION_NO_BODY_PREFIX = "### What happens next \n\n The application will "
        + "be served to relevant people in the case";
    public static final String CONFIDENTIAL_CONFIRMATION_YES_BODY_PREFIX = "### What happens next \n\n The application cannot "
        + "be served. The packs will be sent to the filling team to be redacted.";


    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Confidentiality check event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(serviceOfApplicationService.getConfidentialPacks(
            callbackRequest.getCaseDetails())).build();
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Confidentiality check submitted event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        if (Yes.equals(caseData.getIsAppPackContainConfDetails())
            || Yes.equals(caseData.getIsRespPackContainConfDetails())) {

            log.info("================== Application contain confidential information and will not be served ============");

            return ok(SubmittedCallbackResponse.builder().confirmationBody(
                CONFIDENTIAL_CONFIRMATION_YES_BODY_PREFIX).build());
        }

        log.info("============= Application does not contain confidential information and will be served ===========");

        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        if (caseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(serviceOfApplicationService.sendNotificationsForUnServedPacks(caseData, authorisation)));

        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        caseDataMap.put("finalServedApplicationDetailsList", finalServedApplicationDetailsList);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-all-tabs",
            caseDataMap
        );

        return ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(CONFIDENTIAL_CONFIRMATION_NO_HEADER)
                      .confirmationBody(
            CONFIDENTIAL_CONFIRMATION_NO_BODY_PREFIX).build());
    }


    @PostMapping(path = "/submitted-new", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Confidentiality check submitted event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmittedNew(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        log.info("inside new confidential check submitted event");

        return serviceOfApplicationService.processConfidentialityCheck(authorisation, callbackRequest);
    }


}
