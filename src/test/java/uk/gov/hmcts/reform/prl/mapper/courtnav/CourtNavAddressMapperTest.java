package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CourtNavAddressMapperTest {

    private final CourtNavAddressMapper mapper = Mappers.getMapper(CourtNavAddressMapper.class);

    @Test
    void shouldMapAllFieldsCorrectly() {
        CourtNavAddress courtNavAddress = CourtNavAddress.builder()
            .addressLine1("123 Test Street")
            .addressLine2("Apt 4")
            .addressLine3("Test Block")
            .postTown("London")
            .postCode("E1 6AN")
            .county("Greater London")
            .country("UK")
            .build();

        Address result = mapper.map(courtNavAddress);

        assertNotNull(result);
        assertEquals("123 Test Street", result.getAddressLine1());
        assertEquals("Apt 4", result.getAddressLine2());
        assertEquals("Test Block", result.getAddressLine3());
        assertEquals("London", result.getPostTown());
        assertEquals("E1 6AN", result.getPostCode());
        assertEquals("Greater London", result.getCounty());
        assertEquals("UK", result.getCountry());
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        Address result = mapper.map(null);
        assertNull(result);
    }
}
