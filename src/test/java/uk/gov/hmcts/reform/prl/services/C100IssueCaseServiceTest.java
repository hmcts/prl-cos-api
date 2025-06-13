package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class C100IssueCaseServiceTest {

    @Mock
    private ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    @Mock
    private ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;

    @InjectMocks
    private C100IssueCaseService c100IssueCaseService;

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
    private AllTabServiceImpl allTabsService;

    @Mock
    private SendgridService sendgridService;

    @Mock
    private C100JsonMapper c100JsonMapper;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private DocumentLanguageService documentLanguageService;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private LocationRefDataService locationRefDataService;

    @Mock
    private CourtSealFinderService courtSealFinderService;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private EventService eventPublisher;

    @Mock
    private CaseSummaryTabService caseSummaryTab;

    private static final String AUTH_TOKEN = "Bearer TestAuthToken";

    private static final Map<String, Object> c100DocsMap = new HashMap<>();

    private static DynamicList dynamicList;

    @BeforeEach
    void setUp() {

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C8, "test");
        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C1A, "test");
        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_FINAL, "test");
        c100DocsMap.put(DOCUMENT_FIELD_C8_WELSH, "test");
        c100DocsMap.put(DOCUMENT_FIELD_C1A_WELSH, "test");
        c100DocsMap.put(DOCUMENT_FIELD_FINAL_WELSH, "test");

        dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                      .build()).build();
    }

    @Test
    void testIssueAndSendLocalCourtWithC8() throws Exception {
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
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes)
                                  .build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(List.of(element(ApplicantConfidentialityDetails.builder().build())))
            .childrenConfidentialDetails(List.of(element(ChildConfidentialityDetails.builder().build())))
            .courtList(dynamicList)
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            Map.of("c8Document", "document",
                   "c1ADocument", "document",
                   "c1AWelshDocument", "document",
                   "finalWelshDocument", "document")
        );

        Map<String, Object> objectMap = c100IssueCaseService.issueAndSendToLocalCourt(
            AUTH_TOKEN,
            callbackRequest
        );
        Assertions.assertNotNull(objectMap.get("c8Document"));
        Assertions.assertNotNull(objectMap.get("c1ADocument"));
        Assertions.assertNotNull(objectMap.get("c1AWelshDocument"));
        Assertions.assertNotNull(objectMap.get("finalWelshDocument"));
    }

    @Test
    void testIssueAndSendLocalCourtConditionalFailures() throws Exception {
        CaseData caseData = CaseData.builder()
            .consentOrder(Yes)
            .courtList(dynamicList)
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        Map<String, Object> objectMap = c100IssueCaseService.issueAndSendToLocalCourt(
            AUTH_TOKEN,
            callbackRequest
        );
        Assertions.assertNull(objectMap.get("c8Document"));
        Assertions.assertNull(objectMap.get("c1ADocument"));
        Assertions.assertNull(objectMap.get("c8WelshDocument"));
        Assertions.assertNull(objectMap.get("c1AWelshDocument"));
        Assertions.assertNull(objectMap.get("finalWelshDocument"));
        verifyNoMoreInteractions(organisationService);
    }

    @Test
    void testIssueAndSendLocalCourt() throws Exception {
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
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .consentOrder(YesOrNo.No)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes)
                                  .build())
            .welshLanguageRequirement(YesOrNo.Yes)
            .courtList(dynamicList)
            .welshLanguageRequirementApplication(LanguagePreference.english)
            .languageRequirementApplicationNeedWelsh(YesOrNo.Yes)
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(c100DocsMap);
        Map<String, Object> objectMap = c100IssueCaseService.issueAndSendToLocalCourt(
            AUTH_TOKEN,
            callbackRequest
        );
        Assertions.assertNotNull(objectMap.get("c1ADocument"));
        Assertions.assertNotNull(objectMap.get("c1AWelshDocument"));
        Assertions.assertNotNull(objectMap.get("finalWelshDocument"));
        verify(documentGenService, times(1)).generateDocuments(
            Mockito.anyString(),
            Mockito.any(CaseData.class)
        );
    }

    @Test
    void testIssueAndSendLocalCourtWithC8EmptyList() throws Exception {
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
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes)
                                  .build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(Collections.emptyList())
            .childrenConfidentialDetails(Collections.emptyList())
            .id(123L)
            .courtList(dynamicList)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();


        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            Map.of("c1ADocument", "document",
                   "c1AWelshDocument", "document",
                   "finalWelshDocument", "document")
        );
        Map<String, Object> objectMap = c100IssueCaseService.issueAndSendToLocalCourt(
            AUTH_TOKEN,
            callbackRequest
        );
        Assertions.assertNotNull(objectMap.get("c1ADocument"));
        Assertions.assertNotNull(objectMap.get("c1AWelshDocument"));
        Assertions.assertNotNull(objectMap.get("finalWelshDocument"));
    }

    @Test
    void issueAndSendLocalCourtEventShouldNotifyRpaAndLocalCourt() {
        CaseData caseData = CaseData.builder()
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .consentOrder(No)
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes)
                                  .build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(Collections.emptyList())
            .childrenConfidentialDetails(Collections.emptyList())
            .id(123L)
            .courtList(dynamicList)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        c100IssueCaseService.issueAndSendToLocalCourNotification(callbackRequest);

        verify(eventPublisher,times(2)).publishEvent(Mockito.any());
    }

    @Test
    void issueAndSendLocalCourtEventShouldOnlyNotifyLocalCourt() {
        CaseData caseData = CaseData.builder()
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .consentOrder(Yes)
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes)
                                  .build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(Collections.emptyList())
            .childrenConfidentialDetails(Collections.emptyList())
            .id(123L)
            .courtList(dynamicList)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        c100IssueCaseService.issueAndSendToLocalCourNotification(callbackRequest);

        verify(eventPublisher,times(1)).publishEvent(Mockito.any());
    }

    @Test
    void checkStateIsOfflineWhenUserSelectsNonWorkAllocatedCourt() throws Exception {
        CaseData caseData = CaseData.builder()
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .consentOrder(No)
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes)
                                  .build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(Collections.emptyList())
            .childrenConfidentialDetails(Collections.emptyList())
            .id(123L)
            .courtList(dynamicList)
            .courtId("632")
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        c100IssueCaseService.issueAndSendToLocalCourt(AUTH_TOKEN, callbackRequest);

        assertEquals("Yes", stringObjectMap.get("isNonWorkAllocationEnabledCourtSelected"));
    }

    @Test
    void checkStateIsSubmittedWhenUserSelectsWorkAllocatedCourt() throws Exception {
        DynamicList chosenCourtList = DynamicList.builder().value(DynamicListElement.builder().code("234946:")
                                                                      .label("Swansea Civil Justice Centre - Quay West, Quay Parade - SA1 1SP")
                                                                      .build()).build();
        CaseData caseData = CaseData.builder()
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .consentOrder(No)
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes)
                                  .build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(Collections.emptyList())
            .childrenConfidentialDetails(Collections.emptyList())
            .id(123)
            .courtId("115")
            .courtList(chosenCourtList)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        c100IssueCaseService.issueAndSendToLocalCourt(AUTH_TOKEN, callbackRequest);

        Assertions.assertNull(stringObjectMap.get("isNonWorkAllocationEnabledCourtSelected"));
    }
}
