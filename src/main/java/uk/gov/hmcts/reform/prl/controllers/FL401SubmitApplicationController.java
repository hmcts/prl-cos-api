package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.validators.FL401StatementOfTruthAndSubmitChecker;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_EMAIL_ADDRESS_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
public class FL401SubmitApplicationController {

    @Autowired
    private CourtFinderService courtFinderService;

    @Autowired
    private UserService userService;

    @Autowired
    private AllTabServiceImpl allTabService;

    @Autowired
    private SolicitorEmailService solicitorEmailService;

    @Autowired
    private CaseWorkerEmailService caseWorkerEmailService;

    @Autowired
    private FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    LocationRefDataService locationRefDataService;

    @Autowired
    private CourtFinderApi courtFinderApi;

    @Autowired
    private ConfidentialityTabService confidentialityTabService;

    @PostMapping(path = "/fl401-submit-application-validation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to send FL401 application notification. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application Submitted."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse fl401SubmitApplicationValidation(@RequestHeader("Authorization") @Parameter(hidden = true)
                                                                     String authorisation,
                                                             @RequestBody CallbackRequest callbackRequest) {

        List<String> errorList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        boolean mandatoryEventStatus = fl401StatementOfTruthAndSubmitChecker.hasMandatoryCompleted(caseData);

        if (!mandatoryEventStatus) {
            errorList.add(
                "Statement of truth and submit is not allowed for this case unless you finish all the mandatory events");
        }
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("submitCountyCourtSelection", DynamicList.builder()
            .listItems(locationRefDataService.getCourtLocations(authorisation))
            .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .errors(errorList)
            .build();
    }

    @PostMapping(path = "/fl401-generate-document-submit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate FL401 final document and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application Submitted."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse fl401GenerateDocumentSubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true)  String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        caseData = caseData.toBuilder()
            .solicitorName(userService.getUserDetails(authorisation).getFullName())
            .build();

        final LocalDate localDate = LocalDate.now();

        String baseLocationId = caseData.getSubmitCountyCourtSelection().getValue().getCode();
        String[] venueDetails = locationRefDataService.getCourtDetailsFromEpimmsId(baseLocationId,authorisation).split("-");
        String courtName = Arrays.stream(venueDetails).toArray()[2].toString();
        caseData = caseData.toBuilder().issueDate(localDate).courtName(courtName).build();
        caseData = caseData.toBuilder().isCourtEmailFound("Yes").build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(COURT_NAME_FIELD, courtName);
        String postcode = Arrays.stream(venueDetails).toArray()[3].toString();
        String courtEmail = null;
        if (null != courtFinderApi.findClosestDomesticAbuseCourtByPostCode(postcode)
            && null != courtFinderApi.findClosestDomesticAbuseCourtByPostCode(postcode).getCourts()) {
            String courtSlug = courtFinderApi.findClosestDomesticAbuseCourtByPostCode(postcode).getCourts().get(0).getCourtSlug();
            Court court = courtFinderApi.getCourtDetails(courtSlug);
            caseDataUpdated.put(COURT_ID_FIELD, baseLocationId);
            Optional<CourtEmailAddress> optionalCourtEmail = courtFinderService.getEmailAddress(court);
            if (optionalCourtEmail.isPresent()) {
                courtEmail = optionalCourtEmail.get().getAddress();
            }
        }
        caseDataUpdated.put(COURT_EMAIL_ADDRESS_FIELD, courtEmail);
        String regionName = Arrays.stream(venueDetails).toArray()[4].toString();
        String baseLocationName = Arrays.stream(venueDetails).toArray()[5].toString();
        String regionId = Arrays.stream(venueDetails).toArray()[1].toString();
        caseDataUpdated.put("caseManagementLocation", CaseManagementLocation.builder()
            .regionId(regionId).baseLocationId(baseLocationId).regionName(regionName)
            .baseLocationName(baseLocationName).build());

        Optional<TypeOfApplicationOrders> typeOfApplicationOrders = ofNullable(caseData.getTypeOfApplicationOrders());
        if (typeOfApplicationOrders.isEmpty() || (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)
            && typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder))) {
            caseData = caseData.toBuilder().build();
        } else  if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
            caseData = caseData.toBuilder()
                .respondentBehaviourData(null)
                .build();
        } else if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
            caseData = caseData.toBuilder()
                .home(null)
                .build();
        }
        caseData = caseData.setDateSubmittedDate();

        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));

        caseDataUpdated.put(ISSUE_DATE_FIELD, localDate);

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        caseDataUpdated.put(DATE_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
        caseDataUpdated.put(CASE_DATE_AND_TIME_SUBMITTED_FIELD, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));

        caseDataUpdated.putAll(allTabService.getAllTabsFields(caseData));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    @PostMapping(path = "/fl401-submit-application-send-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to send FL401 application notification. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application Submitted."),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public CallbackResponse fl401SendApplicationNotification(@RequestHeader("Authorization")
                                                                 @Parameter(hidden = true)  String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        UserDetails userDetails = userService.getUserDetails(authorisation);

        try {
            solicitorEmailService.sendEmailToFl401Solicitor(caseDetails, userDetails);
            caseData = caseData.toBuilder()
                .isNotificationSent("Yes")
                .build();

        } catch (Exception e) {
            log.error("Notification could not be sent due to {} ", e.getMessage());
            caseData = caseData.toBuilder()
                .isNotificationSent("No")
                .build();
        }

        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

}
