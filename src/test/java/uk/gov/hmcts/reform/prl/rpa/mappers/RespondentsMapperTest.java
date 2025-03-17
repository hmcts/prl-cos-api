package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import javax.json.JsonValue;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class RespondentsMapperTest {

    @InjectMocks
    RespondentsMapper respondentsMapper;
    @Mock
    AddressMapper addressMapper;

    List<Element<PartyDetails>> respondents;
    Address address;
    Organisation organisation;
    PartyDetails partyDetails;
    Map<String, PartyDetails> respondentSolicitorMap;
    AtomicInteger counter;


    @Before
    public void setUp() {
        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();

        organisation = Organisation.builder().organisationID("").build();


        partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .isDateOfBirthKnown(YesOrNo.Yes)
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(organisation)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(partyDetails).build();
        respondents = Collections.singletonList(partyDetailsElement);
        respondentSolicitorMap = new HashMap<>();

    }

    @Test
    public void testRespondentsMapperEmptyCheck() {
        respondents = Collections.emptyList();;
        assertTrue(respondentsMapper.map(respondents, respondentSolicitorMap).isEmpty());
    }

    @Test
    public void testRespondentsMapperWithAllFields() {
        assertNotNull(respondentsMapper.map(respondents, respondentSolicitorMap));
    }

    @Test
    public void testMapWhenRespondentsIsNull() {
        assertEquals(JsonValue.EMPTY_JSON_ARRAY,respondentsMapper.map(null, respondentSolicitorMap));
    }

    @Test
    public void testMap() {
        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .isDateOfBirthKnown(YesOrNo.Yes)
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .solicitorOrg(organisation)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();
        PartyDetails partyDetails2 = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .isDateOfBirthKnown(YesOrNo.Yes)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .solicitorOrg(organisation)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();
        List<Element<PartyDetails>> respondentsList = new ArrayList<>();
        respondentsList.add(element(partyDetails1));
        respondentsList.add(element(partyDetails2));
        assertNotNull(respondentsMapper.map(respondentsList, respondentSolicitorMap));

    }


}
