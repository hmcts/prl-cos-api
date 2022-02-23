package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;

@RunWith(SpringRunner.class)
public class CallbackControllerTest {


    @Mock
    private ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    @Mock
    private ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;

    @InjectMocks
    private CallbackController callbackController;

    @Mock
    private UserService userService;

    @Mock
    private WorkflowResult workflowResult;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    AllTabServiceImpl allTabsService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String PRL_DRAFT_TEMPLATE = "PRL-DRAFT-C100-20.docx";
    public static final String PRL_C8_TEMPLATE = "PRL-C8-Final-Changes.docx";
    private static final String C100_FINAL_TEMPLATE = "C100-Final-Document.docx";
    public static final String PRL_C1A_TEMPLATE = "PRL-C1A.docx";

    @Before
    public void setUp() {
        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
    }

    @Test
    public void testConfirmMiamApplicationOrExemption() throws WorkflowException {
        CaseDetails caseDetails = CaseDetailsProvider.full();

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.builder().build();


        when(applicationConsiderationTimetableValidationWorkflow.run(callbackRequest))
            .thenReturn(workflowResult);

        callbackController.validateApplicationConsiderationTimetable(callbackRequest);

        verify(applicationConsiderationTimetableValidationWorkflow).run(callbackRequest);
        verifyNoMoreInteractions(applicationConsiderationTimetableValidationWorkflow);

    }

    @Test
    public void testvalidateApplicationConsiderationTimetable() throws WorkflowException {
        CaseDetails caseDetails = CaseDetailsProvider.full();

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.builder().build();


        when(validateMiamApplicationOrExemptionWorkflow.run(callbackRequest))
            .thenReturn(workflowResult);

        callbackController.validateMiamApplicationOrExemption(callbackRequest);

        verify(validateMiamApplicationOrExemptionWorkflow).run(callbackRequest);
        verifyNoMoreInteractions(validateMiamApplicationOrExemptionWorkflow);

    }

    @Test
    public void testGenerateAndStoreDocument() throws Exception {
        //CaseDetails caseDetails  = CaseDetailsProvider.full();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("PRL-DRAFT-C100-20.docx")
                               .build())
            .applicants(applicantList)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
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


        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                    .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
                .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.anyString()))
            .thenReturn(generatedDocumentInfo);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        callbackController.generateAndStoreDocument(authToken, callbackRequest);

        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.anyString()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testIssueAndSendLocalCourt() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .allegationsOfHarmYesNo(YesOrNo.Yes)
            .allegationsOfHarmChildAbductionYesNo(YesOrNo.Yes)
            .allegationsOfHarmDomesticAbuseYesNo(YesOrNo.Yes)
            .allegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
            .build();


        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .c8Document(Document.builder()
                                      .documentUrl(generatedDocumentInfo.getUrl())
                                      .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                      .documentHash(generatedDocumentInfo.getHashToken())
                                      .documentFileName(PRL_C8_TEMPLATE)
                                      .build())
                      .c1ADocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName(PRL_C8_TEMPLATE)
                                       .build())
                      .finalDocument(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName(C100_FINAL_TEMPLATE)
                                         .build())
                      .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.anyString()))
            .thenReturn(generatedDocumentInfo);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController.issueAndSendToLocalCourt(
            authToken,
            callbackRequest
        );
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c8Document"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1ADocument"));
        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.anyString()
        );

        verify(caseWorkerEmailService).sendEmailToCourtAdmin(callbackRequest.getCaseDetails());

    }

    @Test
    public void updateApplicationTest() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren).childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        doNothing().when(allTabsService).updateAllTabs(any(CaseData.class));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        callbackController.updateApplication(authToken, callbackRequest);

        verify(allTabsService, times(1)).updateAllTabs(any(CaseData.class));
    }

    @Test
    public void testSendCaseWithdrawNotification() throws Exception {
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(YesOrNo.Yes)
            .withDrawApplicationReason("Test data")
            .build();

        PartyDetails applicant = PartyDetails.builder().solicitorEmail("test@gmail.com").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);
        CaseData caseData = CaseData.builder()
                           .withDrawApplicationData(withdrawApplication)
                           .applicants(applicantList).build();
        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();

        callbackController.sendEmailNotificationOnCaseWithdraw(authToken, callbackRequest);
        verify(solicitorEmailService, times(1))
            .sendEmailToSolicitor(callbackRequest.getCaseDetails(), userDetails);
    }

    @Test
    public void testSendToGateKeeperNotification() throws Exception {

        PartyDetails applicant1 = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant1).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Child child = Child.builder()
            .isChildAddressConfidential(YesOrNo.No)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String isConfidential = "No";
        if (applicant1.hasConfidentialInfo() || child.hasConfidentialInfo()) {
            isConfidential = "Yes";
        }

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .children(listOfChildren)
            .isCaseUrgent(YesOrNo.No)
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

        callbackController.sendEmailForSendToGatekeeper(authToken, callbackRequest);
        verify(caseWorkerEmailService, times(1))
            .sendEmailToGateKeeper(callbackRequest.getCaseDetails());
    }
}
