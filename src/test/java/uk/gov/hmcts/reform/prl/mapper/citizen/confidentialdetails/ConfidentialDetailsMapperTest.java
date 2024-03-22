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
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

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
    PartyDetails partyDetails3;
    PartyDetails partyDetails4;
    PartyDetails partyDetails5;

    @Test
    public void testChildAndPartyConfidentialDetails() {

        partyDetails1 = PartyDetails.builder()
            .response(Response.builder()
                          .citizenDetails(CitizenDetails
                                              .builder()
                                              .address(Address
                                                           .builder()
                                                           .postCode("test")
                                                           .build())
                                              .contact(Contact
                                                           .builder()
                                                           .email("test")
                                                           .phoneNumber("test")
                                                           .build())
                                              .build())
                          .build())
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
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .currentRespondent(YesOrNo.Yes)
            .build();

        partyDetails4 = PartyDetails.builder()
            .response(Response.builder()
                          .citizenDetails(CitizenDetails
                                              .builder()
                                              .contact(Contact.builder().build())
                                              .address(Address.builder().build())
                                              .build())
                          .build())
            .firstName("ABC 1")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("abc1@xyz.com")
            .phoneNumber("09876543211")
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .currentRespondent(YesOrNo.Yes)
            .build();

        partyDetails3 = PartyDetails.builder()
            .response(Response.builder()
                          .build())
            .firstName("ABC 1")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .currentRespondent(YesOrNo.Yes)
            .build();

        partyDetails5 = PartyDetails.builder()
            .response(Response.builder()
                          .citizenDetails(CitizenDetails
                                              .builder()
                                              .contact(Contact.builder().email("").phoneNumber("").build())
                                              .build())
                          .build())
            .firstName("ABC 1")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .currentRespondent(YesOrNo.Yes)
            .build();

        Element<PartyDetails> partyDetailsFirstRec = Element.<PartyDetails>builder().value(
            partyDetails1).build();
        Element<PartyDetails> partyDetailsSecondRec = Element.<PartyDetails>builder().value(
            partyDetails2).build();
        Element<PartyDetails> partyDetailsThirdRec = Element.<PartyDetails>builder().value(
            partyDetails3).build();
        Element<PartyDetails> partyDetailsFourthRec = Element.<PartyDetails>builder().value(
            partyDetails4).build();
        Element<PartyDetails> partyDetailsFifthRec = Element.<PartyDetails>builder().value(
            partyDetails5).build();
        List<Element<PartyDetails>> listOfPartyDetails = List.of(
            partyDetailsFirstRec,
            partyDetailsSecondRec,
            partyDetailsThirdRec,
            partyDetailsFourthRec,
            partyDetailsFifthRec
        );
        CaseData caseData = CaseData.builder().respondents(listOfPartyDetails).caseTypeOfApplication(C100_CASE_TYPE).build();
        CaseData caseDataCheck = confidentialDetailsMapper.mapConfidentialData(caseData, true);
        assertTrue(
            !caseDataCheck.getRespondentConfidentialDetails().isEmpty()
        );
        assertEquals(partyDetails1.getFirstName(),caseDataCheck.getRespondentConfidentialDetails().get(0).getValue().getFirstName());

    }

    @Test
    public void testChildAndPartyConfidentialDetailsWhenRespondentsNotPresent() {
        CaseData caseData = CaseData.builder().respondents(null).caseTypeOfApplication(C100_CASE_TYPE).build();
        CaseData caseDataCheck = confidentialDetailsMapper.mapConfidentialData(caseData, false);
        assertTrue(caseDataCheck.getRespondentConfidentialDetails().isEmpty());
    }

    @Test
    public void testChildAndPartyConfidentialDetailsF401() {

        PartyDetails partyDetails = PartyDetails.builder()
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

        CaseData caseData = CaseData.builder().respondentsFL401(partyDetails).caseTypeOfApplication(FL401_CASE_TYPE).build();
        CaseData caseDataCheck = confidentialDetailsMapper.mapConfidentialData(caseData, true);
        assertTrue(
            !caseDataCheck.getRespondentConfidentialDetails().isEmpty()
        );
        assertEquals(partyDetails.getFirstName(),caseDataCheck.getRespondentConfidentialDetails().get(0).getValue().getFirstName());

    }

    @Test
    public void testChildAndPartyConfidentialDetailsF401WhenRespondentNotPresent() {
        CaseData caseData = CaseData.builder().respondentsFL401(null).caseTypeOfApplication(FL401_CASE_TYPE).build();
        CaseData caseDataCheck = confidentialDetailsMapper.mapConfidentialData(caseData, true);
        assertTrue(
            caseDataCheck.getRespondentConfidentialDetails().isEmpty()
        );
    }
}
