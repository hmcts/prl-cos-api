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
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        MockitoAnnotations.openMocks(this);

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
                                          "PRL-C100-Draft-Final.docx")).thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(courtFinderService.getNearestFamilyCourt(caseDetails.getCaseData()))
            .thenReturn(court);


        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

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
                                          "PRL-C100-Draft-Final.docx")).thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);

        verify(dgsService).generateDocument(authToken,
                                            callbackRequest.getCaseDetails(),
                                            "PRL-C100-Draft-Final.docx");

    }

    @Test
    public void testFeeDetailsForFeeAmount()  throws Exception {

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(authToken,
                                          callbackRequest.getCaseDetails(),
                                          "PRL-C100-Draft-Final.docx")).thenReturn(generatedDocumentInfo);
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);

        verify(feesService).fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);

    }

    @Test
    public void testCourtDetailsWithCourtName() throws Exception {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseDataForCourt = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        CaseDetails caseDetails1 = CaseDetails.builder()
            .caseData(caseDataForCourt)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails1)
            .build();

        Court court1 = Court.builder()
            .courtName("testcourt")
            .build();

        when(courtFinderService.getNearestFamilyCourt(callbackRequest.getCaseDetails().getCaseData()))
            .thenReturn(court1);

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();;

        CaseData caseData1 = objectMapper.convertValue(
            CaseData.builder()
                .solicitorName(userDetails.getFullName())
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

        when(dgsService.generateDocument(authToken,
                                         callbackRequest.getCaseDetails(),
                                         "PRL-C100-Draft-Final.docx")).thenReturn(generatedDocumentInfo);

        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class))
            .thenReturn(caseData1);

        CallbackResponse callbackResponse = CallbackResponse.builder().data(caseData1).build();

        when(prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest))
            .thenReturn(callbackResponse);
        verify(feesService).fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);

    }

}
