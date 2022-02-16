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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrePopulateFeeAndSolicitorNameController {

    @Autowired
    private FeeService feeService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganisationService organisationService;

    private final CourtFinderService courtLocatorService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DgsService dgsService;


    public static final String PRL_DRAFT_TEMPLATE = "PRL-DRAFT-C100-20.docx";
    private static final String DRAFT_C_100_APPLICATION = "Draft_c100_application.pdf";

    @PostMapping(path = "/getSolicitorAndFeeDetails", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to get Solicitor name and fee amount. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User name received."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse prePoppulateSolicitorAndFees(@RequestHeader("Authorization") String authorisation,
                                                         @RequestBody CallbackRequest callbackRequest) throws Exception {
        List<String> errorList = new ArrayList<>();
        UserDetails userDetails = userService.getUserDetails(authorisation);
        FeeResponse feeResponse = null;
        try {
            feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        } catch (Exception e) {
            errorList.add(e.getMessage());
            return CallbackResponse.builder()
                .errors(errorList)
                .build();
        }
        log.info("=====***** Case Data from CCD before callback *****====== {}", callbackRequest.getCaseDetails().getCaseData());
        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            callbackRequest.getCaseDetails(),
            PRL_DRAFT_TEMPLATE
        );

        List<Element<PartyDetails>> organisationDetailsApplicants = organisationService
            .getRespondentOrganisationDetails(callbackRequest.getCaseDetails().getCaseData());

        List<Element<PartyDetails>> organisationDetailsRespondents = organisationService
            .getRespondentOrganisationDetails(callbackRequest.getCaseDetails().getCaseData());

        CaseData caseData = objectMapper.convertValue(
            CaseData.builder()
                .solicitorName(userDetails.getFullName())
                .userInfo(wrapElements(userService.getUserInfo(authorisation, UserRoles.SOLICITOR)))
                .applicantSolicitorEmailAddress(userDetails.getEmail())
                .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
                .feeAmount(feeResponse.getAmount().toString())
                .submitAndPayDownloadApplicationLink(Document.builder()
                                                         .documentUrl(generatedDocumentInfo.getUrl())
                                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                         .documentHash(generatedDocumentInfo.getHashToken())
                                                         .documentFileName(DRAFT_C_100_APPLICATION).build())
                .courtName(courtLocatorService.getClosestChildArrangementsCourt(callbackRequest.getCaseDetails()
                                                                                    .getCaseData()).getCourtName())

                .applicants(organisationDetailsApplicants)
                .respondents(organisationDetailsRespondents)
                .build(),
            CaseData.class
        );

        log.info("=========CaseData with applicant organisation======= {}", caseData);

        return CallbackResponse.builder()
            .data(caseData)
            .build();

    }
}
