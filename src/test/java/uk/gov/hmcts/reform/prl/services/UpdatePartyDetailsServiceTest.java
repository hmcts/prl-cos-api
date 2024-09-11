package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenResponseDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentC8Document;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.IS_CONFIDENTIAL_DATA_PRESENT;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class UpdatePartyDetailsServiceTest {

    public static final String BEARER_TOKEN = "Bearer token";
    @Mock
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    C100RespondentSolicitorService c100RespondentSolicitorService;

    @Mock
    DocumentLanguageService documentLanguageService;

    @Mock
    DocumentGenService documentGenService;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    @Mock
    ConfidentialityTabService confidentialityTabService;

    @InjectMocks
    UpdatePartyDetailsService updatePartyDetailsService;

    @Test
    public void updateApplicantAndChildNames() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantName", "test1 test22");
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant1).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent).build();

        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent1);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().orgPolicyReference("12345")
            .orgPolicyCaseAssignedRole(null).organisation(null).build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .respondents(respondentList)
            .applicantOrganisationPolicy(organisationPolicy)
            .children(listOfChildren)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> nocMap = Map.of("some", "stuff");
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest,"");
        assertEquals("test1 test22", caseDataUpdated.get("applicantName"));
        assertNotNull(nocMap);

    }

    @Test
    public void updateApplicantAndChildNamesC100withNoApplicants() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantName", "test1 test22");

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(null)
            .children(null)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> nocMap = Map.of("some", "stuff");
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);
        //when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, BEARER_TOKEN);
        assertNotNull(caseDataUpdated.get("applicantName"));
        assertEquals("test1 test22", caseDataUpdated.get("applicantName"));

    }

    @Test
    public void updateApplicantAndChildNamesFL401() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantName", "test1 test22");

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> nocMap = Map.of("some", "stuff");
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);
        //when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, BEARER_TOKEN);
        assertNotNull(caseDataUpdated.get("applicantName"));
        assertEquals("test1 test22", caseDataUpdated.get("applicantName"));

    }

    @Test
    public void updateApplicantAndChildNamesFl401() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantName", "test1 test22");
        caseDataUpdated.put("respondentName", "test1 test22");

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> nocMap = Map.of("some", "stuff");
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        //when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, BEARER_TOKEN);
        assertEquals("test1 test22", caseDataUpdated.get("applicantName"));
        assertEquals("test1 test22", caseDataUpdated.get("respondentName"));

    }

    @Test
    public void testCaseFlagFl401() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantsFL401", "test applicant");
        caseDataUpdated.put("respondentsFL401", "test respondent");
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("applicant")
            .lastName("lastName")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("respondent")
            .lastName("lastName")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
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
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.no)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> nocMap = Map.of("some", "stuff");
        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);

        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, BEARER_TOKEN);

        //final PartyDetails applicantsFL401 = (PartyDetails) caseDataUpdated.get("applicantsFL401");
        //final PartyDetails respondentsFL401 = (PartyDetails) caseDataUpdated.get("respondentsFL401");

        PartyDetails applicantsFL401 = PartyDetails.builder()
            .firstName("test")
            .lastName("applicant")
            .partyLevelFlag(Flags.builder()
                                .partyName("appl party")
                                .build())
            .build();

        PartyDetails respondentsFL401 = PartyDetails.builder()
            .firstName("test")
            .lastName("respondent")
            .partyLevelFlag(Flags.builder()
                                .partyName("resp party")
                                .build())
            .build();

        assertEquals("appl party", applicantsFL401.getPartyLevelFlag().getPartyName());
        assertEquals("resp party", respondentsFL401.getPartyLevelFlag().getPartyName());
    }


    @Test
    public void testCaseFlagApplicantsC100() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantName", "test1 test22");
        caseDataUpdated.put("respondentName", "test1 test22");
        PartyDetails applicant = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isAtAddressLessThan5Years(YesOrNo.Yes)

            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("applicant2")
            .lastName("lastname")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .build();

        Element<PartyDetails> wrappedApplicant1 = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant1).build();

        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant1);
        applicantList.add(wrappedApplicant2);

        PartyDetails respondent = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .isAtAddressLessThan5Years(YesOrNo.Yes)
            .isDateOfBirthKnown(YesOrNo.Yes)
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("test")
            .lastName("lastname")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isCurrentAddressKnown(YesOrNo.No)
            .isAtAddressLessThan5Years(YesOrNo.No)
            .isDateOfBirthKnown(YesOrNo.No)
            .isPlaceOfBirthKnown(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent).build();
        Element<PartyDetails> wrappedRespondent2 = Element.<PartyDetails>builder().value(respondent1).build();

        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent1);
        respondentList.add(wrappedRespondent2);

        caseDataUpdated.put("applicants", "applicantList");
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().orgPolicyReference("12345")
            .orgPolicyCaseAssignedRole("[ApplicantSolicitor]").organisation(Organisation.builder().build()).build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .respondents(respondentList)
            .applicantOrganisationPolicy(organisationPolicy)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> nocMap = Map.of("some", "stuff",
                                            "applicantOrganisationPolicy", organisationPolicy
        );
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);
        //when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, BEARER_TOKEN);
        assertEquals("test1 test22", caseDataUpdated.get("applicantName"));
        assertNotNull(caseDataUpdated.get("applicants"));
    }


    @Test
    public void testCaseFlagRespondentsC100() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantName", "test1 test22");
        caseDataUpdated.put("respondentName", "respondent2 lastname222");
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("respondent1")
            .lastName("lastname1")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("respondent2")
            .lastName("lastname222")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent1).build();
        Element<PartyDetails> wrappedRespondent2 = Element.<PartyDetails>builder().value(respondent2).build();

        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent1);
        respondentList.add(wrappedRespondent2);

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

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.no)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> nocMap = Map.of("some", "stuff");
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, BEARER_TOKEN);
        assertNotNull("respondents");
    }

    @Test
    public void testC8GenerateForRespondentsC100() throws Exception {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantName", "test1 test22");
        caseDataUpdated.put("respondentName", "respondent2 lastname222");
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("respondent1")
            .lastName("lastname1")
            .email("resp1@test.com")
            .address(Address.builder().addressLine1("addressLin1").build())
            .phoneNumber("0123456789")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("respondent2")
            .lastName("lastname222")
            .email("resp1@test.com")
            .address(Address.builder().addressLine1("addressLin1").build())
            .phoneNumber("0123456789")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().id(UUID.randomUUID()).value(respondent1).build();
        Element<PartyDetails> wrappedRespondent2 = Element.<PartyDetails>builder().id(UUID.randomUUID()).value(respondent2).build();

        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent1);
        respondentList.add(wrappedRespondent2);

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

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .respondentC8Document(RespondentC8Document.builder()
                                      .respondentAc8Documents(List
                                                                  .of(Element.<ResponseDocuments>builder().build()))
                                      .build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> nocMap = Map.of("some", "stuff");
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(IS_CONFIDENTIAL_DATA_PRESENT,true);
        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(),Mockito.any(), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService
                 .generateSingleDocument(Mockito.any(),Mockito.any(),Mockito.any(),
                                         Mockito.anyBoolean(),Mockito.anyMap()))
            .thenReturn(Document.builder().build());
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, BEARER_TOKEN);
        assertNotNull("respondents");
    }

    @Test
    public void testC8GenerateForSixRespondentsC100() throws Exception {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("applicantName", "test1 test22");
        caseDataUpdated.put("respondentName", "respondent2 lastname222");
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("respondent1")
            .lastName("lastname1")
            .canYouProvideEmailAddress(YesOrNo.No)
            .email("resp1@test.com")
            .address(Address.builder().addressLine1("addressLin1").build())
            .phoneNumber("0123456789")
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("respondent2")
            .lastName("lastname222")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .email("resp1@test.com")
            .address(Address.builder().addressLine1("addressLin1").build())
            .phoneNumber("0123456789")
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        PartyDetails respondent3 = PartyDetails.builder()
            .firstName("respondent3")
            .lastName("lastname333")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .email("resp1@test.com")
            .address(Address.builder().addressLine1("addressLin1").build())
            .phoneNumber("0123456789")
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        PartyDetails respondent4 = PartyDetails.builder()
            .firstName("respondent4")
            .lastName("lastname444")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .email("resp1@test.com")
            .address(Address.builder().addressLine1("addressLin1").build())
            .phoneNumber("0123456789")
            .build();
        PartyDetails respondent5 = PartyDetails.builder()
            .firstName("respondent5")
            .lastName("lastname555")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("resp1@test.com")
            .address(Address.builder().addressLine1("addressLin1").build())
            .phoneNumber("0123456789")
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        PartyDetails respondent6 = PartyDetails.builder()
            .firstName("respondent6")
            .lastName("lastname666")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .email("resp1@test.com")
            .address(Address.builder().addressLine1("addressLin1").build())
            .phoneNumber("0123456789")
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder()
            .id(UUID.randomUUID()).value(respondent1).build();
        Element<PartyDetails> wrappedRespondent2 = Element.<PartyDetails>builder()
            .id(UUID.randomUUID()).value(respondent2).build();
        Element<PartyDetails> wrappedRespondent3 = Element.<PartyDetails>builder()
            .id(UUID.randomUUID()).value(respondent3).build();
        Element<PartyDetails> wrappedRespondent4 = Element.<PartyDetails>builder()
            .id(UUID.randomUUID()).value(respondent4).build();
        Element<PartyDetails> wrappedRespondent5 = Element.<PartyDetails>builder()
            .id(UUID.randomUUID()).value(respondent5).build();
        Element<PartyDetails> wrappedRespondent6 = Element.<PartyDetails>builder()
            .id(UUID.randomUUID()).value(respondent6).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent1);
        respondentList.add(wrappedRespondent2);
        respondentList.add(wrappedRespondent3);
        respondentList.add(wrappedRespondent4);
        respondentList.add(wrappedRespondent5);
        respondentList.add(wrappedRespondent6);

        List<Element<PartyDetails>> respondentList1 = new ArrayList<>();
        Element<PartyDetails> wrappedRespondent1Changed = Element.<PartyDetails>builder()
            .id(wrappedRespondent1.getId()).value(respondent1.toBuilder().phoneNumber("1234567890")
                                                      .email("test@sd.com")
                                                      .address(Address.builder()
                                                                   .addressLine1("addresdsd2").build())
                                                      .build()).build();
        respondentList1.add(wrappedRespondent1Changed);
        Element<PartyDetails> wrappedRespondent2Changed = Element.<PartyDetails>builder()
            .id(wrappedRespondent2.getId()).value(respondent2.toBuilder().phoneNumber("1234567890")
                                                      .email("test@sd.com")
                                                      .address(Address.builder()
                                                                   .addressLine1("addresdsd2").build())
                                                      .build()).build();
        respondentList1.add(wrappedRespondent2Changed);
        Element<PartyDetails> wrappedRespondent3Changed = Element.<PartyDetails>builder()
            .id(wrappedRespondent3.getId()).value(respondent3.toBuilder().phoneNumber("1234567890")
                                                      .email("test@sd.com")
                                                      .address(Address.builder()
                                                                   .addressLine1("addresdsd2").build())
                                                      .build()).build();
        respondentList1.add(wrappedRespondent3Changed);
        Element<PartyDetails> wrappedRespondent4Changed = Element.<PartyDetails>builder()
            .id(wrappedRespondent4.getId()).value(respondent4.toBuilder().phoneNumber("1234567890")
                                                      .email("test@sd.com")
                                                      .address(Address.builder()
                                                                   .addressLine1("addresdsd2").build())
                                                      .build()).build();
        respondentList1.add(wrappedRespondent4Changed);
        Element<PartyDetails> wrappedRespondent5Changed = Element.<PartyDetails>builder()
            .id(wrappedRespondent5.getId()).value(respondent5.toBuilder().phoneNumber("1234567890")
                                                      .email("test@sd.com")
                                                      .address(Address.builder()
                                                                   .addressLine1("addresdsd2").build())
                                                      .build()).build();
        respondentList1.add(wrappedRespondent5Changed);
        Element<PartyDetails> wrappedRespondent6Changed = Element.<PartyDetails>builder()
            .id(wrappedRespondent6.getId()).value(respondent6.toBuilder().phoneNumber("1234567890")
                                                      .email("test@sd.com")
                                                      .address(Address.builder()
                                                                   .addressLine1("addresdsd2").build())
                                                      .build()).build();
        respondentList1.add(wrappedRespondent6Changed);

        ApplicantConfidentialityDetails applicantConfidentialityDetails = ApplicantConfidentialityDetails.builder()
            .phoneNumber("1234567890")
            .firstName("UserFirst")
            .lastName("UserLast")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .email("test@confidential.com")
            .build();
        Element<ApplicantConfidentialityDetails> applicantConfidential = Element.<ApplicantConfidentialityDetails>builder()
            .value(applicantConfidentialityDetails).build();

        List<Element<ResponseDocuments>> respDoclist = new ArrayList<>();
        respDoclist.add(Element.<ResponseDocuments>builder()
                     .id(UUID.randomUUID())
                     .value(ResponseDocuments.builder()
                                .respondentC8Document(Document.builder().build())
                                .respondentC8DocumentWelsh(Document.builder().build())
                                .build())
                     .build());
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .respondentC8Document(RespondentC8Document.builder()
                                      .respondentAc8Documents(respDoclist).build())
            .build();
        CaseData caseDataChanged = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList1)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        Map<String, Object> stringObjectMap1 = caseDataChanged.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap1, CaseData.class)).thenReturn(caseDataChanged);
        Map<String, Object> nocMap = Map.of("some", "stuff");
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(IS_CONFIDENTIAL_DATA_PRESENT,true);
        when(noticeOfChangePartiesService.generate(caseData, CARESPONDENT)).thenReturn(nocMap);
        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseData);
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(),Mockito.any(), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService
                 .generateSingleDocument(Mockito.any(),Mockito.any(),Mockito.any(),
                                         Mockito.anyBoolean(),Mockito.anyMap()))
            .thenReturn(Document.builder().build());
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(123L)
                                   .data(stringObjectMap1)
                                   .build())
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest, BEARER_TOKEN);
        assertNotNull("respondents");
    }

    @Test
    public void checkIfDetailsChangedFl401All() {
        PartyDetails respondentBefore = PartyDetails.builder()
                .email("test")
                .address(Address.builder()
                        .addressLine1("test")
                        .build())
                .phoneNumber("01234")
                .build();
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("FL401")
                .respondentsFL401(respondentBefore)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        PartyDetails respondent = PartyDetails.builder()
                .email("test1")
                .address(Address.builder()
                        .addressLine1("test1")
                        .build())
                .phoneNumber("012345")
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfDetailsChangedFl401EmailOnly() {
        PartyDetails respondentBefore = PartyDetails.builder()
                .email("test")
                .build();
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("FL401")
                .respondentsFL401(respondentBefore)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        PartyDetails respondent = PartyDetails.builder()
                .email("test1")
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfDetailsChangedFl401AddressOnly() {
        PartyDetails respondentBefore = PartyDetails.builder()
                .address(Address.builder()
                        .addressLine1("test")
                        .build())
                .build();
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("FL401")
                .respondentsFL401(respondentBefore)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        PartyDetails respondent = PartyDetails.builder()
                .address(Address.builder()
                        .addressLine1("test1")
                        .build())
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfDetailsChangedPhoneOnly() {
        PartyDetails respondentBefore = PartyDetails.builder()
                .phoneNumber("01234")
                .build();
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("FL401")
                .respondentsFL401(respondentBefore)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        PartyDetails respondent = PartyDetails.builder()
                .phoneNumber("012345")
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfDetailsChangedFl401NoChange() {
        PartyDetails respondentBefore = PartyDetails.builder()
                .build();
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("FL401")
                .respondentsFL401(respondentBefore)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        PartyDetails respondent = PartyDetails.builder()
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(false, bool);
    }

    @Test
    public void checkIfDetailsChangedC100All() {
        UUID uuid = UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a");
        PartyDetails respondentBefore = PartyDetails.builder()
                .partyId(uuid)
                .email("test")
                .address(Address.builder()
                        .addressLine1("test")
                        .build())
                .phoneNumber("01234")
                .build();
        Element<PartyDetails> wrappedRespondentBefore = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        List<Element<PartyDetails>> listOfRespondents = List.of(wrappedRespondentBefore);
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("C100")
                .respondents(listOfRespondents)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        respondentBefore = respondentBefore.toBuilder()
                .email("test1")
                .address(Address.builder()
                        .addressLine1("test1")
                        .build())
                .phoneNumber("012345")
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfDetailsChangedC100EmailOnly() {
        UUID uuid = UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a");
        PartyDetails respondentBefore = PartyDetails.builder()
                .partyId(uuid)
                .email("test")
                .build();
        Element<PartyDetails> wrappedRespondentBefore = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        List<Element<PartyDetails>> listOfRespondents = List.of(wrappedRespondentBefore);
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("C100")
                .respondents(listOfRespondents)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        respondentBefore = respondentBefore.toBuilder()
                .email("test1")
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfDetailsChangedC100AddressOnly() {
        UUID uuid = UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a");
        PartyDetails respondentBefore = PartyDetails.builder()
                .partyId(uuid)
                .address(Address.builder()
                        .addressLine1("test")
                        .build())
                .build();
        Element<PartyDetails> wrappedRespondentBefore = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        List<Element<PartyDetails>> listOfRespondents = List.of(wrappedRespondentBefore);
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("C100")
                .respondents(listOfRespondents)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        respondentBefore = respondentBefore.toBuilder()
                .address(Address.builder()
                        .addressLine1("test1")
                        .build())
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfDetailsChangedC100PhoneOnly() {
        UUID uuid = UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a");
        PartyDetails respondentBefore = PartyDetails.builder()
                .partyId(uuid)
                .phoneNumber("01234")
                .build();
        Element<PartyDetails> wrappedRespondentBefore = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        List<Element<PartyDetails>> listOfRespondents = List.of(wrappedRespondentBefore);
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("C100")
                .respondents(listOfRespondents)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        respondentBefore = respondentBefore.toBuilder()
                .phoneNumber("012345")
                .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondent);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfDetailsChangedC100NoChange() {
        UUID uuid = UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a");
        PartyDetails respondentBefore = PartyDetails.builder()
                .partyId(uuid)
                .phoneNumber("01234")
                .build();
        Element<PartyDetails> wrappedRespondentBefore = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        List<Element<PartyDetails>> listOfRespondents = List.of(wrappedRespondentBefore);
        CaseData caseDataBefore = CaseData.builder()
                .caseTypeOfApplication("C100")
                .respondents(listOfRespondents)
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetailsBefore(CaseDetails
                        .builder()
                        .data(objectMap)
                        .build())
                .build();
        Map<String, Object> stringObjectMap = callbackRequest.getCaseDetailsBefore().getData();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseDataBefore);
        boolean bool = updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(caseDataBefore, wrappedRespondentBefore);
        assertEquals(false, bool);
    }

    @Test
    public void testUpdateApplicantRespondentAndChildDataCaseTypeEmpty() {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(CaseDetails.builder()
                .data(objectMap)
                .build())
                .build();
        when(confidentialDetailsMapper.mapConfidentialData(
                Mockito.any(CaseData.class),
                Mockito.anyBoolean()
        )).thenReturn(caseData);
        when(objectMapper.convertValue(objectMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> updatedCaseData = updatePartyDetailsService
                .updateApplicantRespondentAndChildData(callbackRequest, "test");
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testGenerateC8DocsAllRespondents() {
        UUID uuid = UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a");
        PartyDetails respondentBefore = PartyDetails.builder()
                .partyId(uuid)
                .phoneNumber("01234")
                .build();
        Element<PartyDetails> wrappedRespondentBefore = Element.<PartyDetails>builder().id(uuid).value(respondentBefore).build();
        List<Element<PartyDetails>> listOfRespondents = new ArrayList<>();
        listOfRespondents.add(wrappedRespondentBefore);
        listOfRespondents.add(wrappedRespondentBefore);
        listOfRespondents.add(wrappedRespondentBefore);
        listOfRespondents.add(wrappedRespondentBefore);
        listOfRespondents.add(wrappedRespondentBefore);
        CaseData caseData = CaseData.builder()
                .caseTypeOfApplication("C100")
                .respondents(listOfRespondents)
                .respondentC8Document(RespondentC8Document.builder().build())
            .citizenResponseDocuments(CitizenResponseDocuments.builder()
                                          .respondentAc8(ResponseDocuments.builder().build())
                                          .respondentBc8(ResponseDocuments.builder().build())
                                          .respondentCc8(ResponseDocuments.builder().build())
                                          .respondentDc8(ResponseDocuments.builder().build())
                                          .respondentEc8(ResponseDocuments.builder().build())
                                          .build())
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(CaseDetails.builder()
                        .data(objectMap)
                        .build())
                .caseDetailsBefore(CaseDetails.builder()
                        .data(objectMap)
                        .build())
                .build();
        when(confidentialDetailsMapper.mapConfidentialData(
                Mockito.any(CaseData.class),
                Mockito.anyBoolean()
        )).thenReturn(caseData);
        when(objectMapper.convertValue(objectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(objectMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> updatedCaseData = updatePartyDetailsService
                .updateApplicantRespondentAndChildData(callbackRequest, "test");
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testSetApplicantDefaultApplicant() {


        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("respondent1")
            .lastName("lastname1")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("respondent2")
            .lastName("lastname222")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent1).build();
        Element<PartyDetails> wrappedRespondent2 = Element.<PartyDetails>builder().value(respondent2).build();

        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent1);
        respondentList.add(wrappedRespondent2);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .build();
        Map<String, Object> updatedCaseData = updatePartyDetailsService.setDefaultEmptyApplicantForC100(caseData);
        assertNotNull(updatedCaseData.get("applicants"));
    }


    @Test
    public void testSetApplicantDefaultApplicant_scenario2() {
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("respondent1")
            .lastName("lastname1")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("respondent2")
            .lastName("lastname222")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent1).build();
        Element<PartyDetails> wrappedRespondent2 = Element.<PartyDetails>builder().value(respondent2).build();

        List<Element<PartyDetails>> applicantsList = new ArrayList<>();
        applicantsList.add(wrappedRespondent1);
        applicantsList.add(wrappedRespondent2);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantsList)
            .build();
        Map<String, Object> updatedCaseData = updatePartyDetailsService.setDefaultEmptyApplicantForC100(caseData);
        assertNotNull(updatedCaseData.get("applicants"));
    }

    @Test
    public void testSetRespondentsDefaultApplicant() {


        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("respondent1")
            .lastName("lastname1")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("respondent2")
            .lastName("lastname222")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent1).build();
        Element<PartyDetails> wrappedRespondent2 = Element.<PartyDetails>builder().value(respondent2).build();

        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent1);
        respondentList.add(wrappedRespondent2);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .build();
        Map<String, Object> updatedCaseData = updatePartyDetailsService.setDefaultEmptyRespondentForC100(caseData);
        assertNotNull(updatedCaseData.get("respondents"));
    }

    @Test
    public void testSetRespondentsDefaultApplicant_scenario2() {
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("respondent1")
            .lastName("lastname1")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("respondent2")
            .lastName("lastname222")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent1).build();
        Element<PartyDetails> wrappedRespondent2 = Element.<PartyDetails>builder().value(respondent2).build();

        List<Element<PartyDetails>> applicantsList = new ArrayList<>();
        applicantsList.add(wrappedRespondent1);
        applicantsList.add(wrappedRespondent2);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantsList)
            .build();
        Map<String, Object> updatedCaseData = updatePartyDetailsService.setDefaultEmptyRespondentForC100(caseData);
        assertNotNull(updatedCaseData.get("respondents"));
    }

    @Test
    public void testSetDefaultEmptyForChildDetails_whenChildDetailsPresent() {
        Child child1 = Child.builder()
            .firstName("Test")
            .lastName("Name1")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Child child2 = Child.builder()
            .firstName("Test")
            .lastName("Name2")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChild1 = Element.<Child>builder().value(child1).build();
        Element<Child> wrappedChild2 = Element.<Child>builder().value(child2).build();

        List<Element<Child>> childList = new ArrayList<>();
        childList.add(wrappedChild1);
        childList.add(wrappedChild2);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .children(childList)
            .build();
        Map<String, Object> updatedCaseData = updatePartyDetailsService.setDefaultEmptyChildDetails(caseData);
        assertEquals(childList, updatedCaseData.get("children"));
    }

    @Test
    public void testSetDefaultEmptyChildDetails_whenNoChildDetailsPresent() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        Map<String, Object> updatedCaseData = updatePartyDetailsService.setDefaultEmptyChildDetails(caseData);
        List<Element<Child>> updatedChildDetails = (List<Element<Child>>) updatedCaseData.get("children");
        assertEquals(1, updatedChildDetails.size());
        assertEquals(Child.builder().build(), updatedChildDetails.get(0).getValue());
    }

    @Test
    public void testSetDefaultEmptyForChildDetails_whenRevisedChildDetailsPresent() {
        ChildDetailsRevised child1 = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name1")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .whoDoesTheChildLiveWith(DynamicList.builder().listItems(new ArrayList<>()).build())
            .build();

        ChildDetailsRevised child2 = ChildDetailsRevised.builder()
            .firstName("Test")
            .lastName("Name2")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .parentalResponsibilityDetails("test")
            .build();

        Element<ChildDetailsRevised> wrappedChild1 = Element.<ChildDetailsRevised>builder().value(child1).build();
        Element<ChildDetailsRevised> wrappedChild2 = Element.<ChildDetailsRevised>builder().value(child2).build();

        List<Element<ChildDetailsRevised>> childList = new ArrayList<>();
        childList.add(wrappedChild1);
        childList.add(wrappedChild2);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .taskListVersion(PrlAppsConstants.TASK_LIST_VERSION_V3)
            .newChildDetails(childList)
            .build();
        Map<String, Object> updatedCaseData = updatePartyDetailsService.setDefaultEmptyChildDetails(caseData);
        assertNotNull(updatedCaseData.get("newChildDetails"));
    }

    @Test
    public void testSetDefaultEmptyChildDetails_whenNoRevisedChildDetailsPresent() {
        PartyDetails applicant = PartyDetails.builder().firstName("test").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);

        PartyDetails respondent = PartyDetails.builder().firstName("test")
            .address(Address
                .builder()
                .build()).lastName("test").build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent);

        PartyDetails otherParties = PartyDetails.builder().firstName("test")
            .address(Address.builder().addressLine1("test").build()).lastName("test").build();
        Element<PartyDetails> wrappedOtherParties = Element.<PartyDetails>builder().value(otherParties).build();
        PartyDetails otherParties2 = PartyDetails.builder().firstName("test")
            .address(Address.builder().addressLine1("test").addressLine2("test").build()).lastName("test").build();
        Element<PartyDetails> wrappedOtherParties2 = Element.<PartyDetails>builder().value(otherParties2).build();
        PartyDetails otherParties3 = PartyDetails.builder().firstName("test")
            .address(Address.builder().addressLine1("test").postCode("test").build()).lastName("test").build();
        Element<PartyDetails> wrappedOtherParties3 = Element.<PartyDetails>builder().value(otherParties3).build();
        List<Element<PartyDetails>> otherPartiesList = new ArrayList<>();
        PartyDetails otherParties4 = PartyDetails.builder().firstName("test")
            .address(Address.builder().addressLine1("test").postCode("test").addressLine2("test").build()).lastName("test").build();
        Element<PartyDetails> wrappedOtherParties4 = Element.<PartyDetails>builder().value(otherParties4).build();
        otherPartiesList.add(wrappedOtherParties);
        otherPartiesList.add(wrappedOtherParties2);
        otherPartiesList.add(wrappedOtherParties3);
        otherPartiesList.add(wrappedOtherParties4);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .taskListVersion(PrlAppsConstants.TASK_LIST_VERSION_V2)
            .applicants(applicantList)
            .respondents(respondentList)
            .otherPartyInTheCaseRevised(otherPartiesList)
            .build();

        Map<String, Object> updatedCaseData = updatePartyDetailsService.setDefaultEmptyChildDetails(caseData);
        List<Element<ChildDetailsRevised>> updatedChildDetails = (List<Element<ChildDetailsRevised>>) updatedCaseData.get("newChildDetails");
        assertEquals(1, updatedChildDetails.size());
    }
}
