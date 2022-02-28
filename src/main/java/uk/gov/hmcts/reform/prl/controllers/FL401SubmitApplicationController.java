package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_EMAIL_ADDRESS_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
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
    private ObjectMapper objectMapper;

    @Autowired
    private DgsService dgsService;

    @Autowired
    private OrganisationService organisationService;

    @PostMapping(path = "/fl401-generate-document-submit-application", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate FL401 final document and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Application Submitted."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse fl401GenerateDocumentSubmitApplication(
        @RequestHeader("Authorization") String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Court nearestDomesticAbuseCourt = courtFinderService
            .getNearestFamilyCourt(CaseUtils.getCaseData(caseDetails, objectMapper));

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("Generating the Final document of FL401 for case id " + caseData.getId());
        final LocalDate localDate = LocalDate.now();
        caseData = caseData.toBuilder().issueDate(localDate).courtName((nearestDomesticAbuseCourt != null)
                                                                ? nearestDomesticAbuseCourt
            .getCourtName() : "").build();

        caseDataUpdated.put(COURT_NAME_FIELD, nearestDomesticAbuseCourt != null
            ? nearestDomesticAbuseCourt.getCourtName() : "");

        caseData = organisationService.getApplicantOrganisationDetailsForFL401(caseData);

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

        GeneratedDocumentInfo generatedDocumentC8Info = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
            DA_C8_TEMPLATE
        );
        log.info("Generated DA C8 Document");

        caseDataUpdated.put(C8_DOCUMENT_FIELD, Document.builder()
            .documentUrl(generatedDocumentC8Info.getUrl())
            .documentBinaryUrl(generatedDocumentC8Info.getBinaryUrl())
            .documentHash(generatedDocumentC8Info.getHashToken())
            .documentFileName(DA_C8_DOC).build());
        caseDataUpdated.put(ISSUE_DATE_FIELD, localDate);

        Optional<CourtEmailAddress> matchingEmailAddress = courtFinderService
            .getEmailAddress(nearestDomesticAbuseCourt);

        caseDataUpdated.put(COURT_EMAIL_ADDRESS_FIELD, (nearestDomesticAbuseCourt != null
            && matchingEmailAddress.isPresent()) ? matchingEmailAddress.get().getAddress() :
            Objects.requireNonNull(nearestDomesticAbuseCourt).getCourtEmailAddresses().get(0).getAddress());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    @PostMapping(path = "/fl401-submit-application-send-notification", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
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

        solicitorEmailService.sendEmailToFl401Solicitor(caseDetails, userDetails);
        caseWorkerEmailService.sendEmailToFl401LocalCourt(caseDetails, caseData.getCourtEmailAddress());

        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }
}
