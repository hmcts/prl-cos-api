package uk.gov.hmcts.reform.prl.services.bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.BundleApiClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.bundle.BundleCreateRequestMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Bundle;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class BundlingServiceTest {
    @Mock
    private BundleApiClient bundleApiClient;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseUtils caseUtils;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private BundlingService bundlingService;
    private BundleCreateResponse bundleCreateResponse;
    private CaseDetails caseDetails;
    private CaseData caseData;

    CaseData c100CaseData;
    CaseData c100CaseDataOther;

    CaseData c100CaseData2;
    AllegationOfHarm allegationOfHarmYes;
    Map<String, Object> caseDataMap;

    @Before
    public void setUp() {
        allegationOfHarmYes = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(Yes).build();

        List<FurtherEvidence> furtherEvidences = new ArrayList<>();
        furtherEvidences.add(FurtherEvidence.builder().typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.consentOrder)
            .documentFurtherEvidence(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample1.pdf").build())
            .restrictCheckboxFurtherEvidence(new ArrayList<>()).build());

        List<OtherDocuments> otherDocuments = new ArrayList<>();
        otherDocuments.add(OtherDocuments.builder().documentName("Application docu")
            .documentOther(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample2.pdf").build()).documentTypeOther(
                DocTypeOtherDocumentsEnum.applicantStatement).restrictCheckboxOtherDocuments(new ArrayList<>()).build());

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home homefull = Home.builder()
            .address(Address.builder().addressLine1("123").build())
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(YesOrNo.No)
            .doAnyChildrenLiveAtAddress(YesOrNo.Yes)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .isPropertyRented(YesOrNo.No)
            .isThereMortgageOnProperty(YesOrNo.No)
            .isPropertyAdapted(YesOrNo.No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();

        List<ContactInformation> contactInformationList = Collections.singletonList(ContactInformation.builder()
            .addressLine1("29, SEATON DRIVE")
            .addressLine2("test line")
            .townCity("NORTHAMPTON")
            .postCode("NN3 9SS")
            .build());

        Organisations organisations = Organisations.builder()
            .organisationIdentifier("79ZRSOU")
            .name("Civil - Organisation 2")
            .contactInformation(contactInformationList)
            .build();

        PartyDetails partyDetailsWithOrganisations = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .isAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .solicitorOrg(Organisation.builder()
                .organisationID("79ZRSOU")
                .organisationName("Civil - Organisation 2")
                .build())
            .organisations(organisations)
            .build();
        Element<PartyDetails> applicants = Element.<PartyDetails>builder().value(partyDetailsWithOrganisations).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(applicants);
        OtherPersonConfidentialityDetails otherPersonConfidentialityDetails = uk.gov.hmcts.reform.prl.models.complextypes.confidentiality
            .OtherPersonConfidentialityDetails.builder()
            .isPersonIdentityConfidential(Yes)
            .firstName("test1")
            .lastName("last1")
            .relationshipToChildDetails("uncle")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .build();
        Element<OtherPersonConfidentialityDetails> otherPerson = Element.<OtherPersonConfidentialityDetails>builder()
            .value(otherPersonConfidentialityDetails).build();
        List<Element<OtherPersonConfidentialityDetails>> otherPersonList = Collections.singletonList(otherPerson);


        ChildConfidentialityDetails childConfidentialityDetails = ChildConfidentialityDetails.builder()
            .firstName("ChildFirst")
            .lastName("ChildLast")
            .otherPerson(otherPersonList)
            .build();

        Element<ChildConfidentialityDetails> childConfidential = Element.<ChildConfidentialityDetails>builder()
            .value(childConfidentialityDetails).build();
        List<Element<ChildConfidentialityDetails>> childConfidentialList = Collections.singletonList(childConfidential);
        ApplicantConfidentialityDetails applicantConfidentialityDetails = ApplicantConfidentialityDetails.builder()
            .phoneNumber("1234567890")
            .firstName("UserFirst")
            .lastName("UserLast")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .email("test@confidential.com")
            .build();

        Element<ApplicantConfidentialityDetails> applicantConfidential = Element.<ApplicantConfidentialityDetails>builder()
            .value(applicantConfidentialityDetails).build();

        List<Element<ApplicantConfidentialityDetails>> applicantConfidentialList = Collections.singletonList(
            applicantConfidential);

        List<Bundle> bundleList = new ArrayList<>();
        bundleList.add(Bundle.builder().value(BundleDetails.builder().stitchedDocument(DocumentLink.builder().build())
            .stitchStatus("DONE").stitchedDocument(DocumentLink.builder().build()).build()).build());

        c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(No)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(allegationOfHarmYes)
            .applicants(listOfApplicants)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            //.allegationsOfHarmYesNo(No)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .bundleInformation(BundlingInformation.builder().build())
            //.home(homefull)
            .build();
        bundleCreateRequestMapper = new BundleCreateRequestMapper();

        c100CaseDataOther = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(Yes)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(allegationOfHarmYes)
            .applicants(listOfApplicants)
            .state(State.DECISION_OUTCOME)
            //.allegationsOfHarmYesNo(No)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .finalWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("finalWelshDoc.pdf").build())
            .c1AWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C1AWelshDoc.pdf").build())
            .bundleInformation(BundlingInformation.builder().caseBundles(bundleList).build())
            //.home(homefull)
            .build();

        c100CaseData2 = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(allegationOfHarmYes)
            .applicants(listOfApplicants)
            .state(State.DECISION_OUTCOME)
            //.allegationsOfHarmYesNo(No)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .finalWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("finalWelshDoc.pdf").build())
            .c1AWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C1AWelshDoc.pdf").build())
            .bundleInformation(BundlingInformation.builder().caseBundles(bundleList).build())
            //.home(homefull)
            .build();
    }

    @Test
    public void testCreateBundleService() throws Exception {
        when(authTokenGenerator.generate()).thenReturn("authToken");
        BundleCreateResponse bundleCreateResponse = BundleCreateResponse.builder().documentTaskId(123).build();
        when(bundlingService.createBundleServiceRequest(c100CaseData,"eventId","authorization"))
            .thenReturn(bundleCreateResponse);
        BundleCreateResponse expectedResponse = bundlingService.createBundleServiceRequest(c100CaseData,"eventId","authorization");
        assertEquals(bundleCreateResponse.documentTaskId, expectedResponse.documentTaskId);
    }

    @Test
    public void testCreateBundleServiceWhenLanguagePreferenceWelshAsYes() throws Exception {
        when(authTokenGenerator.generate()).thenReturn("authToken");
        BundleCreateResponse bundleCreateResponse = BundleCreateResponse.builder().documentTaskId(123).build();
        when(bundlingService.createBundleServiceRequest(c100CaseData,"eventId","authorization"))
            .thenReturn(bundleCreateResponse);
        BundleCreateResponse expectedResponse = bundlingService.createBundleServiceRequest(c100CaseDataOther,"eventId","authorization");
        assertEquals(bundleCreateResponse.documentTaskId, expectedResponse.documentTaskId);
    }

    @Test
    public void testCreateBundleServiceWhenLanguagePreferenceWelshNotSet() throws Exception {
        when(authTokenGenerator.generate()).thenReturn("authToken");
        BundleCreateResponse bundleCreateResponse = BundleCreateResponse.builder().documentTaskId(123).build();
        when(bundlingService.createBundleServiceRequest(c100CaseData2,"eventId","authorization"))
            .thenReturn(bundleCreateResponse);
        BundleCreateResponse expectedResponse = bundlingService.createBundleServiceRequest(c100CaseDataOther,"eventId","authorization");
        assertEquals(bundleCreateResponse.documentTaskId, expectedResponse.documentTaskId);
    }
}
