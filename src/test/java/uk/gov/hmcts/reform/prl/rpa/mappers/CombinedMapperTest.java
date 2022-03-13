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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.json.JsonArray;

import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class CombinedMapperTest {

    @InjectMocks
    CombinedMapper combinedMapper;
    @Mock
    ApplicantsMapper applicantsMapper;
    @Mock
    SolicitorsMapper solicitorsMapper;

    @InjectMocks
    AddressMapper addressMapper;


    @Mock
    RespondentsMapper respondentsMapper;

    List<Element<PartyDetails>> respondents;
    Address address;
    Organisation organisation;
    PartyDetails partyDetails;
    Map<String, PartyDetails> respondentSolicitorMap;
    AtomicInteger counter;
    public JsonArray respondentArray;


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
    public void testWithNullValues() {
        CaseData caseData = CaseData.builder().build();
        assertNull(combinedMapper.map(caseData));
    }

}
