package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class FeeAndPayServiceRequestController extends AbstractCallbackController {

    public static final String C100_DEFAULT_BASE_LOCATION_NAME = "STOKE ON TRENT TRIBUNAL HEARING CENTRE";
    public static final String C100_DEFAULT_BASE_LOCATION_ID = "283922";
    public static final String C100_DEFAULT_REGION_NAME = "Midlands";
    public static final String C100_DEFAULT_REGION_ID = "2";
    private final PaymentRequestService paymentRequestService;
    private final ConfidentialityTabService confidentialityTabService;
    private final CaseSummaryTabService caseSummaryTab;
    private final DocumentGenService documentGenService;

    @PostMapping(path = "/test-payment-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create Fee and Pay service request . Returns service request reference if "
        + "successful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> ccdSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        log.info("Before {}", callbackRequest);
        return ok(SubmittedCallbackResponse.builder().confirmationHeader("TEST").confirmationBody(
            "YOU ARE SEEING CONFIRMATION PAGE").build());
    }

    @PostMapping(path = "/generate-document-submit-application-test", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse generateDocumentSubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = callbackRequest.getCaseDetails().getCaseData();

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        Map<String, Object> caseDataUpdated = caseData.toMap(CcdObjectMapper.getObjectMapper());
        caseDataUpdated.put(
            CASE_DATE_AND_TIME_SUBMITTED_FIELD,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime)
        );
        caseData = caseData
            .toBuilder()
            .applicantsConfidentialDetails(
                confidentialityTabService
                    .getConfidentialApplicantDetails(
                        caseData.getApplicants().stream()
                            .map(
                                Element::getValue)
                            .collect(
                                Collectors.toList())))
            .childrenConfidentialDetails(confidentialityTabService.getChildrenConfidentialDetails(
                caseData.getChildren()
                    .stream()
                    .map(Element::getValue)
                    .collect(
                        Collectors.toList()))).state(
                State.SUBMITTED_NOT_PAID)
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .build();

        Map<String, Object> map = documentGenService.generateDocuments(authorisation, caseData);
        // updating Summary tab to update case status
        caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
        caseDataUpdated.putAll(map);

        if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            // updating Summary tab to update case status
            caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
            caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));
            caseDataUpdated.putAll(documentGenService.generateDraftDocuments(authorisation, caseData));
        }

        //Assign default court to all c100 cases for work allocation.
        caseDataUpdated.put("caseManagementLocation", CaseManagementLocation.builder()
            .regionId(C100_DEFAULT_REGION_ID)
            .baseLocationId(C100_DEFAULT_BASE_LOCATION_ID).regionName(C100_DEFAULT_REGION_NAME)
            .baseLocationName(C100_DEFAULT_BASE_LOCATION_NAME).build());
        PaymentServiceResponse paymentServiceResponse = paymentRequestService.createServiceRequest(
            callbackRequest,
            authorisation
        );
        caseDataUpdated.put(
            "paymentServiceRequestReferenceNumber",
            paymentServiceResponse.getServiceRequestReference()
        );
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
