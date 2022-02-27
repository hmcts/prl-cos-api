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
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401SubmitApplicationController {

    private static final String FL401_FINAL_TEMPLATE = "FL401-Final.docx";
    private static final String FL401_FINAL_DOC = "FL401FinalDocument.pdf";

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

    @PostMapping(path = "/fl401-generate-document-submit-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate FL401 final document and submit application. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Application Submitted."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse fl401GenerateDocumentSubmitApplication(@RequestHeader("Authorization") String authorisation,
                                        @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Court closestDomesticAbuseCourt = courtFinderService
            .getClosestChildArrangementsCourt(CaseUtils.getCaseData(caseDetails, objectMapper));

        log.info("Generating the Final document of FL401 ");
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        final LocalDate localDate = LocalDate.now();
        caseData = caseData.toBuilder().issueDate(localDate).courtName((closestDomesticAbuseCourt != null)
                                                                ? closestDomesticAbuseCourt
            .getCourtName() : "").build();

        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
            FL401_FINAL_TEMPLATE
        );
        log.info("Generated FL401 Document");

        Optional<CourtEmailAddress> matchingEmailAddress = courtFinderService.getEmailAddress(closestDomesticAbuseCourt);

        CaseData caseDataUpdated = objectMapper.convertValue(
            CaseData.builder()
                .courtName((closestDomesticAbuseCourt != null)  ? closestDomesticAbuseCourt.getCourtName() : "No Court Fetched")
                .courtEmailAddress((closestDomesticAbuseCourt != null && matchingEmailAddress.isPresent())
                                       ? matchingEmailAddress.get().getAddress() :
                                       Objects.requireNonNull(closestDomesticAbuseCourt).getCourtEmailAddresses().get(0).getAddress())
                .issueDate(localDate)
                .finalDocument(Document.builder()
                                   .documentUrl(generatedDocumentInfo.getUrl())
                                   .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                   .documentHash(generatedDocumentInfo.getHashToken())
                                   .documentFileName(FL401_FINAL_DOC).build())
                .build(),
            CaseData.class
        );

        //todo document generation

        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/fl401-submit-application-send-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to send FL401 application notification. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Application Submitted."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse fl401SendApplicationNotification(@RequestHeader("Authorization") String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        //todo document generation

        return CallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }
}
