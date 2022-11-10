package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.CaseFlag;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.APPLICANT_FLAG;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.RESPONDENT_FLAG;

@RunWith(MockitoJUnitRunner.class)
public class SearchCasesDataServiceTest {

    @InjectMocks
    SearchCasesDataService searchCasesDataService;
    @Mock
    ObjectMapper objectMapper;

    @Test
    public void updateApplicantAndChildNames() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
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
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .children(listOfChildren)
            .build();

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);
        searchCasesDataService.updateApplicantAndChildNames(objectMapper,caseDataUpdated);
        assertEquals("test1 test22", caseDataUpdated.get("applicantName"));


    }


    @Test
    public void updateApplicantAndChildNamesC100withNoApplicants() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(null)
            .children(null)
            .build();

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);
        searchCasesDataService.updateApplicantAndChildNames(objectMapper,caseDataUpdated);
        assertNull(caseDataUpdated.get("applicantName"));
    }

    @Test
    public void updateApplicantAndChildNamesFL401() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);
        searchCasesDataService.updateApplicantAndChildNames(objectMapper,caseDataUpdated);
        assertNull(caseDataUpdated.get("applicantName"));
    }

    @Test
    public void updateApplicantAndChildNamesFl401() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
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

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);
        searchCasesDataService.updateApplicantAndChildNames(objectMapper,caseDataUpdated);
        assertEquals("test1 test22", caseDataUpdated.get("applicantName"));
        assertEquals("test1 test22", caseDataUpdated.get("respondentName"));

    }

    @Test
    public void testCaseFlagApplicantsC100() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        PartyDetails applicant = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);
        searchCasesDataService.updateApplicantAndChildNames(objectMapper,caseDataUpdated);
        assertEquals("test1 test22", caseDataUpdated.get("applicantName"));
        final CaseFlag applicantFlag = (CaseFlag) caseDataUpdated.get(APPLICANT_FLAG);
        assertNotNull(caseDataUpdated.get(APPLICANT_FLAG));
        assertEquals("test1 test22", applicantFlag.getPartyName());
        assertEquals(PartyEnum.applicant.getDisplayedValue(), applicantFlag.getRoleOnCase());
    }

    @Test
    public void testCaseFlagRespondentsC100() {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        PartyDetails respondent = PartyDetails.builder()
            .firstName("respondent")
            .lastName("lastname")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .respondents(respondentList)
            .build();

        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);
        searchCasesDataService.updateApplicantAndChildNames(objectMapper, caseDataUpdated);
        final CaseFlag respondentFlag = (CaseFlag) caseDataUpdated.get(RESPONDENT_FLAG);
        assertNotNull(respondentFlag);
        assertEquals("respondent lastname", respondentFlag.getPartyName());
        assertEquals(PartyEnum.respondent.getDisplayedValue(), respondentFlag.getRoleOnCase());
    }


}
