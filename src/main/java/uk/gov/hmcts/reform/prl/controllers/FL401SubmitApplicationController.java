package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.validators.FL401StatementOfTruthAndSubmitChecker;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_EMAIL_ADDRESS_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401SubmitApplicationController {

    private static final String FL401_FINAL_TEMPLATE = "FL401-Final.docx";
    private static final String FL401_FINAL_DOC = "FL401FinalDocument.pdf";
    private static final String DA_C8_TEMPLATE = "PRL-DA-C8.docx";
    private static final String DA_C8_DOC = "C8_Document.pdf";

    @Autowired
    private CourtFinderService courtFinderService;

    @Autowired
    private UserService userService;

    @Autowired
    private SolicitorEmailService solicitorEmailService;

    @Autowired
    private CaseWorkerEmailService caseWorkerEmailService;

    @Autowired
    private FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DgsService dgsService;

    @PostMapping(path = "/fl401-submit-application-validation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to send FL401 application notification. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Application Submitted."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse fl401SubmitApplicationValidation(@RequestHeader("Authorization")
                                                                 String authorisation,
                                                             @RequestBody CallbackRequest callbackRequest) {

        List<String> errorList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        boolean mandatoryEventStatus = fl401StatementOfTruthAndSubmitChecker.hasMandatoryCompleted(caseData);
        if (!mandatoryEventStatus) {
            errorList.add(
                "Statement of Truth and submit is not allowed for this case unless you finish all the mandatory events");
        }
        return CallbackResponse.builder()
            .errors(errorList)
            .build();
    }

    @PostMapping(path = "/fl401-generate-document-submit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate FL401 final document and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Application Submitted."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse fl401GenerateDocumentSubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Court nearestDomesticAbuseCourt = courtFinderService
            .getNearestFamilyCourt(CaseUtils.getCaseData(caseDetails, objectMapper));
        log.info("Retrieved court Name ==> {}", (null != nearestDomesticAbuseCourt ? nearestDomesticAbuseCourt.getCourtName()
            : "No Court Name Fetched"));

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        final LocalDate localDate = LocalDate.now();
        caseData = caseData.toBuilder().issueDate(localDate).courtName((nearestDomesticAbuseCourt != null)
                                                                ? nearestDomesticAbuseCourt
            .getCourtName() : "").build();

        Optional<CourtEmailAddress> courtEmailAddress = courtFinderService
            .getEmailAddress(nearestDomesticAbuseCourt);

        if (courtEmailAddress.isPresent()) {
            caseData = caseData.toBuilder().isCourtEmailFound("Yes").build();
        } else {
            caseData = caseData.toBuilder().isCourtEmailFound("No").build();
        }

        Optional<TypeOfApplicationOrders> typeOfApplicationOrders = ofNullable(caseData.getTypeOfApplicationOrders());

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(COURT_NAME_FIELD, nearestDomesticAbuseCourt != null
            ? nearestDomesticAbuseCourt.getCourtName() : "");
        caseDataUpdated.put(COURT_EMAIL_ADDRESS_FIELD, (nearestDomesticAbuseCourt != null
            && courtEmailAddress.isPresent()) ? courtEmailAddress.get().getAddress() :
            Objects.requireNonNull(nearestDomesticAbuseCourt).getCourtEmailAddresses().get(0).getAddress());

        if (typeOfApplicationOrders.isEmpty() || (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)
            && typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder))) {
            caseData = caseData.toBuilder().build();
            log.info("Case date with Home ----{}---- and respondent bahaviour === {} =====",
                     caseData.getHome(), caseData.getRespondentBehaviourData());
        } else  if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
            caseData = caseData.toBuilder()
                .respondentBehaviourData(null)
                .build();
            log.info("Case date with respondent bahaviour === {} =====", caseData.getRespondentBehaviourData());
        } else if (typeOfApplicationOrders.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
            caseData = caseData.toBuilder()
                .home(null)
                .build();
            log.info("Case date with home details === {} =====", caseData.getHome());
        }

        log.info("Generating the Final document of FL401 for case id " + caseData.getId());
        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
            FL401_FINAL_TEMPLATE
        );
        log.info("Generated FL401 Document");

        caseDataUpdated.put(FINAL_DOCUMENT_FIELD, Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(FL401_FINAL_DOC).build());
        caseDataUpdated.put(ISSUE_DATE_FIELD, localDate);

        GeneratedDocumentInfo generatedDocumentC8Info = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
            DA_C8_TEMPLATE
        );
        log.info("Generated DA C8 Document");

        caseDataUpdated.put(DOCUMENT_FIELD_C8, Document.builder()
            .documentUrl(generatedDocumentC8Info.getUrl())
            .documentBinaryUrl(generatedDocumentC8Info.getBinaryUrl())
            .documentHash(generatedDocumentC8Info.getHashToken())
            .documentFileName(DA_C8_DOC).build());
        caseDataUpdated.put(ISSUE_DATE_FIELD, localDate);

        if (null != generatedDocumentC8Info && null != generatedDocumentInfo) {
            caseData = caseData.toBuilder().isDocumentGenerated("Yes").build();
        } else {
            caseData = caseData.toBuilder().isDocumentGenerated("No").build();
        }

        log.info(" Court email flag {} and document generation flag {}",
                 caseData.getIsCourtEmailFound(), caseData.getIsDocumentGenerated());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    @PostMapping(path = "/fl401-submit-application-send-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to send FL401 application notification. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Application Submitted."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse fl401SendApplicationNotification(@RequestHeader("Authorization")
                                                                     String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest)
        throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        UserDetails userDetails = userService.getUserDetails(authorisation);

        try {
            solicitorEmailService.sendEmailToFl401Solicitor(caseDetails, userDetails);
            caseWorkerEmailService.sendEmailToFl401LocalCourt(caseDetails, caseData.getCourtEmailAddress());

            caseData = caseData.toBuilder()
                .isNotificationSent("Yes")
                .build();
        } catch (Exception e) {
            log.error("Notification could not be sent due to {} ", e.getMessage());
            caseData = caseData.toBuilder()
                .isNotificationSent("No")
                .build();
        }

        log.info(" email notification flag{}", caseData.getIsNotificationSent());

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        caseData = caseData.toBuilder()
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .build();

        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }
}
