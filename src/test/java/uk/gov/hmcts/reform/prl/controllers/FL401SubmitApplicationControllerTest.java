package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.validators.FL401StatementOfTruthAndSubmitChecker;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class FL401SubmitApplicationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private FL401SubmitApplicationController fl401SubmitApplicationController;

    @Mock
    private UserService userService;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private UserDetails userDetails;

    @Mock
    AllTabServiceImpl allTabsService;

    @Mock
    CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    SolicitorEmailService solicitorEmailService;

    @Mock
    FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

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

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .description("Horsham Court")
            .explanation("Family")
            .build();

        court = Court.builder()
            .courtName("testcourt")
            .courtEmailAddresses(Collections.singletonList(courtEmailAddress))
            .build();

        userDetails = UserDetails.builder()
            .email("solicitor@example.com")
            .surname("userLast")
            .build();
    }

    @Test
    public void testSubmitApplicationEventValidation() throws Exception {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        String applicantNames = "TestFirst TestLast";

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("localcourt@test.com")
            .dateSubmitted(String.valueOf("22-02-2022"))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(caseData)
            .errors(Collections.singletonList("test"))
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(fl401StatementOfTruthAndSubmitChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        fl401SubmitApplicationController.fl401SubmitApplicationValidation(authToken, callbackRequest);
    }

    @Test
    public void testCourtNameAndEmailAddressReturnedWhileFamilyEmailAddressReturned() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("PRL-DRAFT-C100-20.docx")
                               .build())
            .applicantsFL401(applicant)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .draftOrderDoc(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName("PRL-DRAFT-C100-20.docx")
                                         .build())
                      .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                      .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.anyString()))
            .thenReturn(generatedDocumentInfo);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Court closestDomesticAbuseCourt = courtFinderService.getNearestFamilyCourt(
            CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper));
        Optional<CourtEmailAddress> matchingEmailAddress = courtFinderService.getEmailAddress(closestDomesticAbuseCourt);

        when(courtFinderService.getNearestFamilyCourt(CaseUtils.getCaseData(callbackRequest.getCaseDetails(),
                                                                            objectMapper)))
            .thenReturn(court);
        fl401SubmitApplicationController.fl401GenerateDocumentSubmitApplication(authToken, callbackRequest);

        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.anyString()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testFl401SendApplicationNotification() throws Exception {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        String applicantNames = "TestFirst TestLast";

        String isConfidential = "No";
        if (fl401Applicant.getCanYouProvideEmailAddress().equals(YesOrNo.Yes)
            || (fl401Applicant.getIsEmailAddressConfidential() != null
            && fl401Applicant.getIsEmailAddressConfidential().equals(YesOrNo.Yes))
            || (fl401Applicant.hasConfidentialInfo())) {
            isConfidential = "Yes";
        }

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("localcourt@test.com")
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        fl401SubmitApplicationController.fl401SendApplicationNotification(authToken, callbackRequest);
        verify(caseWorkerEmailService, times(1))
            .sendEmailToFl401LocalCourt(callbackRequest.getCaseDetails(), caseData.getCourtEmailAddress());
        verify(solicitorEmailService,times(1)).sendEmailToFl401Solicitor(callbackRequest.getCaseDetails(), userDetails);
    }
}
