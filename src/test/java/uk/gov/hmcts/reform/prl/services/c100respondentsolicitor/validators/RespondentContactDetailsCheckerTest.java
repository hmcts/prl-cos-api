package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.AddressHistory;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RespondentContactDetailsCheckerTest {

    @InjectMocks
    RespondentContactDetailsChecker respondentContactDetailsChecker;

    CaseData caseData;

    CaseData noAddressData;

    @Before
    public void setUp() {

        Address address = Address.builder()
            .addressLine1("test")
            .postCode("test")
            .build();

        Element<Address> wrappedAddress = Element.<Address>builder().value(address).build();
        List<Element<Address>> addressList = Collections.singletonList(wrappedAddress);

        PartyDetails respondent = PartyDetails.builder()
            .response(Response.builder()
                          .citizenDetails(CitizenDetails
                                              .builder()
                                              .firstName("Test")
                                              .lastName("Test")
                                              .dateOfBirth(LocalDate.of(2000, 8, 20))
                                              .address(address)
                                              .contact(Contact.builder()
                                                           .email("Test")
                                                           .phoneNumber("0785544").build())
                                              .addressHistory(AddressHistory.builder()
                                                                  .isAtAddressLessThan5Years(No)
                                                                  .previousAddressHistory(addressList)
                                                                  .build())
                                              .build())
                          .activeRespondent(Yes)
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        Address noAddress = Address.builder()
            .build();

        PartyDetails noAddressRespondent = PartyDetails.builder()
            .response(Response.builder()
                          .citizenDetails(CitizenDetails
                                              .builder()
                                              .firstName("Test")
                                              .lastName("Test")
                                              .dateOfBirth(LocalDate.of(2000, 8, 20))
                                              .address(noAddress)
                                              .contact(Contact.builder()
                                                           .email("Test")
                                                           .phoneNumber("0785544").build())
                                              .addressHistory(AddressHistory.builder()
                                                                  .isAtAddressLessThan5Years(No)
                                                                  .previousAddressHistory(addressList)
                                                                  .build())
                                              .build())
                          .activeRespondent(Yes)
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(noAddressRespondent).build();
        List<Element<PartyDetails>> noAddressRespondentList = Collections.singletonList(wrappedRespondent);

        noAddressData = CaseData.builder().respondents(noAddressRespondentList).build();

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {
        Boolean bool = respondentContactDetailsChecker.isStarted(caseData);
        assertTrue(bool);
    }

    @Test
    public void mandatoryInformationTest() {
        Boolean bool = respondentContactDetailsChecker.isFinished(caseData);
        assertTrue(bool);
    }

    @Test
    public void noAddressTest() {
        Boolean bool = respondentContactDetailsChecker.isFinished(noAddressData);
        assertFalse(bool);
    }
}
