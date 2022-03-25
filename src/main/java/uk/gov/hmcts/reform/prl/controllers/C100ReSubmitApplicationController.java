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
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100ReSubmitApplicationController {

    @Value("${document.templates.c100.c100_final_template}")
    protected String c100FinalTemplate;

    @Value("${document.templates.c100.c100_final_filename}")
    protected String c100FinalFilename;

    @Value("${document.templates.c100.c100_c8_template}")
    protected String c100C8Template;

    @Value("${document.templates.c100.c100_c8_filename}")
    protected String c100C8Filename;

    @Value("${document.templates.c100.c100_c1a_template}")
    protected String c100C1aTemplate;

    @Value("${document.templates.c100.c100_c1a_filename}")
    protected String c100C1aFilename;

    @Value("${document.templates.c100.c100_final_welsh_template}")
    protected String c100FinalWelshTemplate;

    @Value("${document.templates.c100.c100_final_welsh_filename}")
    protected String c100FinalWelshFilename;

    @Value("${document.templates.c100.c100_c8_welsh_template}")
    protected String c100C8WelshTemplate;

    @Value("${document.templates.c100.c100_c8_welsh_filename}")
    protected String c100C8WelshFilename;

    @Value("${document.templates.c100.c100_c1a_welsh_template}")
    protected String c100C1aWelshTemplate;

    @Value("${document.templates.c100.c100_c1a_welsh_filename}")
    protected String c100C1aWelshFilename;


    @Autowired
    private CourtFinderService courtFinderService;

    @Autowired
    private UserService userService;

    @Autowired
    private SolicitorEmailService solicitorEmailService;

    @Autowired
    private CaseWorkerEmailService caseWorkerEmailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DgsService dgsService;

    @Autowired
    private CaseEventService caseEventService;

    @Autowired
    private DocumentLanguageService documentLanguageService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    AllTabServiceImpl allTabService;

    @PostMapping(path = "/resubmit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to change the state and document generation and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Resubmission completed"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse resubmitApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));
        Optional<String> previousStates = eventsForCase.stream().map(CaseEventDetail::getStateId).filter(
            C100ReSubmitApplicationController::getPreviousState).findFirst();
        Map<String, Object> caseDataUpdated = new HashMap<>(caseDetails.getData());
        if (previousStates.isPresent()) {
            // For submitted state - No docs will be generated.
            if (State.SUBMITTED_PAID.getValue().equalsIgnoreCase(previousStates.get())) {
                caseData = caseData.toBuilder().state(State.SUBMITTED_PAID).build();
                caseDataUpdated.put("state", State.SUBMITTED_PAID);
                ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
                caseData = caseData.setDateSubmittedAndIssueDate();
                caseDataUpdated.put("dateSubmitted", caseData.getDateSubmitted());
                caseDataUpdated.put("dateAndTimeSubmitted", DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
                caseWorkerEmailService.sendEmail(caseDetails);
                solicitorEmailService.sendEmail(caseDetails);
            }
            // For Case issue state - All docs will be regenerated.
            if (State.CASE_ISSUE.getValue().equalsIgnoreCase(previousStates.get())) {
                caseData = organisationService.getApplicantOrganisationDetails(caseData);
                caseData = organisationService.getRespondentOrganisationDetails(caseData);
                caseData = caseData.setIssueDate();
                caseData = caseData.toBuilder().state(State.CASE_ISSUE).build();

                DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
                generateDocuments(authorisation, caseData, caseDataUpdated, documentLanguage);
                caseDataUpdated.put("state", State.CASE_ISSUE);
                caseDataUpdated.put(PrlAppsConstants.ISSUE_DATE_FIELD, caseData.getIssueDate());
                caseWorkerEmailService.sendEmailToCourtAdmin(callbackRequest.getCaseDetails());
            }

            caseDataUpdated.putAll(allTabService.getAllTabsFields(caseData));
            caseDataUpdated.put("confidentialityDisclaimerSubmit", Map.of("confidentialityChecksChecked", null));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    private static boolean getPreviousState(String eachState) {
        return (!eachState.equalsIgnoreCase(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()))
            && (eachState.equalsIgnoreCase(State.SUBMITTED_PAID.getValue())
            || eachState.equalsIgnoreCase(State.CASE_ISSUE.getValue()));
    }

    private Map<String, Object> generateDocuments(String authorisation, CaseData caseData,
                                                  Map<String, Object> caseDataUpdated,
                                   DocumentLanguage documentLanguage) throws Exception {
        if (documentLanguage.isGenEng()) {
            caseDataUpdated.put(DOCUMENT_FIELD_C8, generateDocumentField(
                c100C8Filename,
                generateDocument(
                    authorisation,
                    c100C8Template,
                    caseData,
                    false
                )
            ));

            if (caseData.getAllegationsOfHarmYesNo().equals(YesOrNo.Yes)) {
                caseDataUpdated.put(DOCUMENT_FIELD_C1A, generateDocumentField(
                    c100C1aFilename,
                    generateDocument(
                        authorisation,
                        c100C1aTemplate,
                        caseData,
                        false
                    )
                ));
            }

            caseDataUpdated.put(DOCUMENT_FIELD_FINAL, generateDocumentField(
                c100FinalFilename,
                generateDocument(
                    authorisation,
                    c100FinalTemplate,
                    caseData,
                    false
                )
            ));

        }
        if (documentLanguage.isGenWelsh()) {
            caseDataUpdated.put(DOCUMENT_FIELD_C8_WELSH, generateDocumentField(
                c100C8WelshFilename,
                generateDocument(
                    authorisation,
                    c100C8WelshTemplate,
                    caseData,
                    true
                )
            ));

            if (caseData.getAllegationsOfHarmYesNo().equals(YesOrNo.Yes)) {
                caseDataUpdated.put(DOCUMENT_FIELD_C1A_WELSH, generateDocumentField(
                    c100C1aWelshFilename,
                    generateDocument(
                        authorisation,
                        c100C1aWelshTemplate,
                        caseData,
                        true
                    )
                ));
            }
            caseDataUpdated.put(DOCUMENT_FIELD_FINAL_WELSH, generateDocumentField(
                c100FinalWelshFilename,
                generateDocument(
                    authorisation,
                    c100FinalWelshTemplate,
                    caseData,
                    true
                )
            ));

        }

        return caseDataUpdated;
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
        caseData = caseData.toBuilder().isDocumentGenerated("No").build();
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
        }

        log.info("Genereated the {} document for case id {} ", template, caseData.getId());
        return generatedDocumentInfo;
    }

}
