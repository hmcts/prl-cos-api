package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.json.JsonObject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantsMapperTest {

    @InjectMocks
    ApplicantsMapper applicantsMapper;
    @Mock
    AddressMapper addressMapper;
    @Mock
    Organisation organisation;
    PartyDetails partyDetails;
    Address address;
    List<Element<PartyDetails>> applicants;
    JsonObject applicantSolicitorMap1;
    HashMap<String, PartyDetails> applicantSolicitorMap;
    AtomicInteger counter = new AtomicInteger();

    @Before
    public void setup() {

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
            .solicitorOrg(organisation)
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(partyDetails).build();
        applicants = Collections.singletonList(partyDetailsElement);

        applicantSolicitorMap = new HashMap<String, PartyDetails>();
        //applicantSolicitorMap.put()
    }

    @Test
    public void testApplicantsMapperMap() {
        assertNotNull(applicantsMapper.map(applicants, applicantSolicitorMap));
    }

    @Test
    public void testIfApplicantsNull() {
        applicants = null;
        assertEquals(Collections.emptyList(),applicantsMapper.map(applicants, applicantSolicitorMap));
    }

    @Test
    public void testIfApplicantsIsEmpty() {
        applicants = Collections.emptyList();
        assertTrue(applicantsMapper.map(applicants, applicantSolicitorMap).isEmpty());
    }

}
