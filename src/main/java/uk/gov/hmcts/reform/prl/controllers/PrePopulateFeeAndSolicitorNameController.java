package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.validators.SubmitAndPayChecker;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@RestController
public class PrePopulateFeeAndSolicitorNameController {

    @Autowired
    private FeeService feeService;

    @Autowired
    private UserService userService;

    @Autowired
    SubmitAndPayChecker submitAndPayChecker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DgsService dgsService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private DocumentLanguageService documentLanguageService;


    @Value("${document.templates.c100.c100_draft_template}")
    protected String c100DraftTemplate;

    @Value("${document.templates.c100.c100_draft_filename}")
    protected String c100DraftFilename;

    @Value("${document.templates.c100.c100_draft_welsh_template}")
    protected String c100DraftWelshTemplate;

    @Value("${document.templates.c100.c100_draft_welsh_filename}")
    protected String c100DraftWelshFilename;

    @Value("${southampton.court.email-address}")
    protected String southamptonCourtEmailAddress;

    public static final String CURRENCY_SIGN_POUND = "£";

    @PostMapping(path = "/getSolicitorAndFeeDetails", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to get Solicitor name and fee amount. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User name received."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse prePopulateSolicitorAndFees(@RequestHeader("Authorization") String authorisation,
                                                        @RequestBody CallbackRequest callbackRequest) throws Exception {
        List<String> errorList = new ArrayList<>();
        CaseData caseData = null;
        boolean mandatoryEventStatus = submitAndPayChecker.hasMandatoryCompleted(callbackRequest
                                                                                     .getCaseDetails().getCaseData());

        if (!mandatoryEventStatus) {
            errorList.add(
                "Submit and pay is not allowed for this case unless you finish all the mandatory events");
        } else {
            FeeResponse feeResponse = null;
            try {
                feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
            } catch (Exception e) {
                errorList.add(e.getMessage());
                return CallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }
            CaseData caseDataForOrgDetails = callbackRequest.getCaseDetails().getCaseData();
            caseDataForOrgDetails = organisationService.getApplicantOrganisationDetails(caseDataForOrgDetails);
            caseDataForOrgDetails = organisationService.getRespondentOrganisationDetails(caseDataForOrgDetails);

            UserDetails userDetails = userService.getUserDetails(authorisation);
            caseData = CaseData.builder()
                .solicitorName(userDetails.getFullName())
                .userInfo(wrapElements(userService.getUserInfo(authorisation, UserRoles.SOLICITOR)))
                .applicantSolicitorEmailAddress(userDetails.getEmail())
                .caseworkerEmailAddress(southamptonCourtEmailAddress)
                .feeAmount(CURRENCY_SIGN_POUND + feeResponse.getAmount().toString())
                .build();

            caseData = buildGeneratedDocumentCaseData(authorisation, callbackRequest, caseData, caseDataForOrgDetails);

            log.info("Saving Court name into DB..");
        }

        return CallbackResponse.builder()
            .data(caseData)
            .errors(errorList)
            .build();
    }

    private CaseData buildGeneratedDocumentCaseData(
        @RequestHeader("Authorization") String authorisation,
        @RequestBody CallbackRequest callbackRequest,
        CaseData caseData,
        CaseData caseDataForOrgDetails)
        throws Exception {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(callbackRequest.getCaseDetails().getCaseData());

        if (documentLanguage.isGenEng()) {
            GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseDataForOrgDetails).build(),
                c100DraftTemplate
            );

            caseData = caseData.toBuilder().isEngDocGen(documentLanguage.isGenEng() ? Yes.toString() : No.toString())
                .submitAndPayDownloadApplicationLink(Document.builder()
                                                         .documentUrl(generatedDocumentInfo.getUrl())
                                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                         .documentHash(generatedDocumentInfo.getHashToken())
                                                         .documentFileName(c100DraftFilename).build()).build();
        }

        if (documentLanguage.isGenWelsh()) {
            GeneratedDocumentInfo generatedWelshDocumentInfo = dgsService.generateWelshDocument(
                authorisation,
                callbackRequest.getCaseDetails(),
                c100DraftWelshTemplate
            );

            caseData = caseData.toBuilder().isWelshDocGen(documentLanguage.isGenWelsh() ? Yes.toString() : No.toString())
                .submitAndPayDownloadApplicationWelshLink(Document.builder()
                                                              .documentUrl(generatedWelshDocumentInfo.getUrl())
                                                              .documentBinaryUrl(generatedWelshDocumentInfo.getBinaryUrl())
                                                              .documentHash(generatedWelshDocumentInfo.getHashToken())
                                                              .documentFileName(c100DraftWelshFilename).build()).build();
        }
        return caseData;
    }
}
