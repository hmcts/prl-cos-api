package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class PrePopulateFeeAndSolicitorNameControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PrePopulateFeeAndSolicitorNameController prePopulateFeeAndSolicitorNameController;

    @Mock
    private UserService userService;

    @Mock
    private DgsService dgsService;
    @Mock
    private FeeService feesService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private Court court;

    @Mock
    private CaseDetails caseDetails;

    @Mock
    private CaseData caseData;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();

        feeResponse = FeeResponse.builder()
            .amount(BigDecimal.valueOf(232.00))
            .build();

        caseData = CaseData.builder()
            .courtName("testcourt")
            .build();

        caseDetails = CaseDetails.builder()
            .caseId("123")
            .caseData(caseData)
            .build();

        court = Court.builder()
            .courtName("testcourt")
            .build();
    }

    //TODO Update this testcase once we have integration with Fee and Pay
    @Test
    public void testUserDetailsForSolicitorName() throws Exception {

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(authToken,
                                          callbackRequest.getCaseDetails(),
                                          "PRL-DRAFT-C100-20.docx")).thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(courtFinderService.getClosestChildArrangementsCourt(caseDetails.getCaseData()))
            .thenReturn(court);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenThrow(new RuntimeException("Cannot process"));

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);

        verify(userService).getUserDetails(authToken);

    }

    @Test
    public void testWhenControllerCalledOneInvokeToDgsService() throws Exception {

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(authToken,
                                          callbackRequest.getCaseDetails(),
                                          "PRL-DRAFT-C100-20.docx")).thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(courtFinderService.getClosestChildArrangementsCourt(caseDetails.getCaseData()))
            .thenThrow(new RuntimeException("Cannot process"));

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);

        verify(dgsService).generateDocument(authToken,
                                            callbackRequest.getCaseDetails(),
                                            "PRL-DRAFT-C100-20.docx");

    }

    @Test
    public void testFeeDetailsForFeeAmount()  throws Exception {

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(authToken,
                                          callbackRequest.getCaseDetails(),
                                          "PRL-DRAFT-C100-20.docx")).thenReturn(generatedDocumentInfo);
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(courtFinderService.getClosestChildArrangementsCourt(caseDetails.getCaseData()))
            .thenThrow(new RuntimeException("Cannot process"));

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);

        verify(feesService).fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);

    }

    @Test (expected = NullPointerException.class)
    public void testCourtDetailsWithCourtName() throws Exception {

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(authToken,
                                         callbackRequest.getCaseDetails(),
                                         "PRL-DRAFT-C100-20.docx")).thenReturn(generatedDocumentInfo);

        Court court1 = courtFinderService.getClosestChildArrangementsCourt(caseDetails.getCaseData());
        when(courtFinderService.getClosestChildArrangementsCourt(caseDetails.getCaseData()))
            .thenReturn(court1);
        CaseData caseData1 = objectMapper.convertValue(
            CaseData.builder()
                .solicitorName("example")
                .applicantSolicitorEmailAddress("test@gmail.com")
                .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
                .feeAmount(feeResponse.getAmount().toString())
                .submitAndPayDownloadApplicationLink(Document.builder()
                                                         .documentUrl(generatedDocumentInfo.getUrl())
                                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                         .documentHash(generatedDocumentInfo.getHashToken())
                                                         .documentFileName("Draft_c100_application.pdf").build())
                .courtName(court1.getCourtName())
                .build(),
            CaseData.class
        );

        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class))
            .thenReturn(caseData1);

        CallbackResponse callbackResponse = CallbackResponse.builder().data(caseData1).build();

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);
        when(prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest))
            .thenReturn(callbackResponse);
        verify(feesService).fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);

    }

}
