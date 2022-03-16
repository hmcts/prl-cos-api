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

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(organisation)
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


}
