package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class RefugeConfidentialityServiceTest {

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    RefugeConfidentialityService refugeConfidentialityService;

    @Test
    public void testSetConfidentialFlagForPartiesLiveInRefuge() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .liveInRefuge(YesOrNo.Yes)
            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("applicant2")
            .lastName("lastname")
            .liveInRefuge(YesOrNo.No)
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


        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        refugeConfidentialityService.setConfidentialFlagForPartiesLiveInRefuge(stringObjectMap);
        List<Element<PartyDetails>> updatedApplicantList = (List<Element<PartyDetails>>) stringObjectMap.get("applicants");
        assertEquals(YesOrNo.Yes, updatedApplicantList.get(0).getValue().getIsAddressConfidential());
        assertEquals(YesOrNo.No,updatedApplicantList.get(1).getValue().getIsAddressConfidential());
    }

    @Test
    public void testUpdateConfidentialDetailsForRefuge() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .liveInRefuge(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("applicant2")
            .lastName("lastname")
            .liveInRefuge(YesOrNo.No)
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


        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.AMEND_APPLICANTS_DETAILS.getId())
            .caseDetails(CaseDetails
                             .builder()
                             .data(stringObjectMap)
                             .build())
            .build();

        stringObjectMap = refugeConfidentialityService.updateConfidentialDetailsForRefuge(callbackRequest);
        List<Element<PartyDetails>> updatedApplicantList = (List<Element<PartyDetails>>) stringObjectMap.get("applicants");
        assertNull(updatedApplicantList.get(0).getValue().getIsAddressConfidential());
        assertEquals(YesOrNo.No,updatedApplicantList.get(1).getValue().getIsAddressConfidential());
    }
}
