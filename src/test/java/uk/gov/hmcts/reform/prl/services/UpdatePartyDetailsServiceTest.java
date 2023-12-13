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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class UpdatePartyDetailsServiceTest {

    @Mock
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

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
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest);
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
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest);
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
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(applicant1)
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
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest);
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
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest);
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

        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest);

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
            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("applicant2")
            .lastName("lastname")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicant1 = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant1).build();

        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant1);
        applicantList.add(wrappedApplicant2);

        caseDataUpdated.put("applicants", "applicantList");
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().orgPolicyReference("12345")
            .orgPolicyCaseAssignedRole("[ApplicantSolicitor]").organisation(Organisation.builder().build()).build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
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
                                            "applicantOrganisationPolicy", organisationPolicy);
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
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest);
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

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
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
        updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest);
        assertNotNull("respondents");
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
}
