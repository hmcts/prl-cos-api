package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ApplicantsMapperTest {

    @InjectMocks
    private ApplicantsMapper applicantsMapper;

    @Mock
    private AddressMapper addressMapper;

    private HashMap<String, PartyDetails> applicantSolicitorMap;

    @Test
    void testApplicantsMapperMap() {

        Address address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .solicitorOrg(Organisation.builder().organisationID("").build())
            .build();

        List<Element<PartyDetails>> applicants = List.of(Element.<PartyDetails>builder().value(partyDetails).build());
        applicantSolicitorMap = new HashMap<>();

        assertNotNull(applicantsMapper.map(applicants, applicantSolicitorMap));
    }

    @Test
    void testIfApplicantsNull() {
        assertEquals(Collections.emptyList(), applicantsMapper.map(null, applicantSolicitorMap));
    }

    @Test
    void testIfApplicantsIsEmpty() {
        assertTrue(applicantsMapper.map(Collections.emptyList(), applicantSolicitorMap).isEmpty());
    }

}
