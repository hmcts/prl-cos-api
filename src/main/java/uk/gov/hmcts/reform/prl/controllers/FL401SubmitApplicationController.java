package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401SubmitApplicationController {

    @Value("${document.templates.fl401.fl401_final_template}")
    protected String fl401FinalTemplate;

    @Value("${document.templates.fl401.fl401_final_filename}")
    protected String fl401FinalFilename;

    @Value("${document.templates.fl401.fl401_final_welsh_template}")
    protected String fl401FinalWelshTemplate;

    @Value("${document.templates.fl401.fl401_final_welsh_filename}")
    protected String fl401FinalWelshFilename;

    @Value("${document.templates.fl401.fl401_c8_template}")
    protected String fl401C8Template;

    @Value("${document.templates.fl401.fl401_c8_filename}")
    protected String fl401C8Filename;

    @Value("${document.templates.fl401.fl401_c8_welsh_template}")
    protected String fl401C8WelshTemplate;

    @Value("${document.templates.fl401.fl401_c8_welsh_filename}")
    protected String fl401C8WelshFilename;

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

    @Autowired
    private DocumentLanguageService documentLanguageService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    private AllTabServiceImpl allTabService;

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
                "Statement of truth and submit is not allowed for this case unless you finish all the mandatory events");
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

        if (nearestDomesticAbuseCourt != null) {
            caseDataUpdated.put(COURT_NAME_FIELD,nearestDomesticAbuseCourt.getCourtName());
            caseDataUpdated.put(COURT_ID_FIELD, String.valueOf(nearestDomesticAbuseCourt.getCountyLocationCode()));
        }

        caseDataUpdated.put(COURT_EMAIL_ADDRESS_FIELD, (nearestDomesticAbuseCourt != null
            && courtEmailAddress.isPresent()) ? courtEmailAddress.get().getAddress() :
            Objects.requireNonNull(nearestDomesticAbuseCourt).getCourtEmailAddresses().get(0).getAddress());

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

        log.info("Generating the Final document of FL401 for case id " + caseData.getId());
        log.info("Issue date for the application: {} ", caseData.getIssueDate());

        log.info("Calling org service to update the org address .. for case id {} ", caseData.getId());
        caseData = organisationService.getApplicantOrganisationDetailsForFL401(caseData);
        log.info("Called org service to update the org address .. for case id {} ", caseData.getId());

        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);

        if (documentLanguage.isGenEng()) {
            caseDataUpdated.put("isEngDocGen", Yes.toString());
            caseDataUpdated.put(FINAL_DOCUMENT_FIELD,
                                generateDocumentField(fl401FinalFilename,generateDocument(authorisation, fl401FinalTemplate, caseData,
                                                                       false)));
            caseDataUpdated.put(DOCUMENT_FIELD_C8,
                                generateDocumentField(fl401C8Filename,generateDocument(authorisation, fl401C8Template, caseData,
                                                                       false)));
        }

        if (documentLanguage.isGenWelsh()) {
            caseDataUpdated.put("isWelshDocGen", Yes.toString());
            caseDataUpdated.put(DOCUMENT_FIELD_FINAL_WELSH,
                                generateDocumentField(fl401FinalWelshFilename,generateDocument(authorisation, fl401FinalWelshTemplate,
                                                                       caseData, true)));
            caseDataUpdated.put(DOCUMENT_FIELD_C8_WELSH,
                                generateDocumentField(fl401C8WelshFilename,generateDocument(authorisation, fl401C8WelshTemplate, caseData,
                                                                       true)));
        }
        caseDataUpdated.put(ISSUE_DATE_FIELD, localDate);

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        caseDataUpdated.put(DATE_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));

        allTabService.updateAllTabsIncludingConfTab(caseData);

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
                                                                   @RequestBody CallbackRequest callbackRequest) {

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

        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    private Document generateDocumentField(String fileName,GeneratedDocumentInfo generatedDocumentInfo) {
        if (null == generatedDocumentInfo) {
            return null;
        }
        return Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(fileName).build();
    }

    private GeneratedDocumentInfo generateDocument(String authorisation, String template, CaseData caseData,
                                                   boolean isWelsh)
        throws Exception {
        log.info("Generating the {} document for case id {} ", template, caseData.getId());
        GeneratedDocumentInfo generatedDocumentInfo = null;
        if (isWelsh) {
            generatedDocumentInfo = dgsService.generateWelshDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                template
            );
        } else {
            generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                template
            );
        }
        if (null != generatedDocumentInfo) {
            caseData = caseData.toBuilder().isDocumentGenerated("Yes").build();
        } else {
            caseData = caseData.toBuilder().isDocumentGenerated("No").build();
        }

        log.info("Generated the {} document for case id {} ", template, caseData.getId());
        return generatedDocumentInfo;
    }

}
