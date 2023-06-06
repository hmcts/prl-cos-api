package uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class ConfidentialDetailsMapperTest {

    @InjectMocks
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    AllTabServiceImpl allTabsService;

    Address address;
    PartyDetails partyDetails1;
    PartyDetails partyDetails2;
    PartyDetails partyDetailsCheck1;
    PartyDetails partyDetailsCheck2;

    @Test
    public void testChildAndPartyConfidentialDetails() {

        partyDetails1 = PartyDetails.builder()
            .firstName("ABC 1")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("abc1@xyz.com")
            .phoneNumber("09876543211")
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .currentRespondent(YesOrNo.Yes)
            .build();

        partyDetails2 = PartyDetails.builder()
            .firstName("ABC 2")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .phoneNumber("12345678900")
            .email("abc2@xyz.com")
            .currentRespondent(YesOrNo.Yes)
            .build();

        partyDetailsCheck1 = PartyDetails.builder()
            .firstName("ABC 1")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("abc1@xyz.com")
            .phoneNumber("09876543211")
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .currentRespondent(YesOrNo.Yes)
            .build();

        partyDetailsCheck2 = PartyDetails.builder()
            .firstName("ABC 2")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(null)
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .phoneNumber("12345678900")
            .email("abc2@xyz.com")
            .currentRespondent(YesOrNo.Yes)
            .build();

        Element<PartyDetails> partyDetailsFirstRec = Element.<PartyDetails>builder().value(
            partyDetails1).build();
        Element<PartyDetails> partyDetailsSecondRec = Element.<PartyDetails>builder().value(
            partyDetails2).build();
        List<Element<PartyDetails>> listOfPartyDetails = List.of(
            partyDetailsFirstRec,
            partyDetailsSecondRec
        );

        Element<PartyDetails> partyDetailsFirstRecCheck = Element.<PartyDetails>builder().value(
            partyDetailsCheck1).build();
        Element<PartyDetails> partyDetailsSecondRecCheck = Element.<PartyDetails>builder().value(
            partyDetailsCheck2).build();
        List<Element<PartyDetails>> listOfPartyDetailsCheck = List.of(
            partyDetailsFirstRecCheck,
            partyDetailsSecondRecCheck
        );

        CaseData caseData = CaseData.builder().respondents(listOfPartyDetails).caseTypeOfApplication(C100_CASE_TYPE).build();
        CaseData caseDataCheck = confidentialDetailsMapper.mapConfidentialData(caseData, true);
        assertTrue(
            !caseDataCheck.getRespondentConfidentialDetails().isEmpty()
        );
        assertEquals(partyDetails1.getFirstName(),caseDataCheck.getRespondentConfidentialDetails().get(0).getValue().getFirstName());

    }
}
