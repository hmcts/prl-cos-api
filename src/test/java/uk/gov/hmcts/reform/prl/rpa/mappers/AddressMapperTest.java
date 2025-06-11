package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Address;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
public class AddressMapperTest {

    @InjectMocks
    AddressMapper addressMapper;


    @Test
    public void testAddressMapperWithoutNullValues() {
        Address address = Address.builder().addressLine1("157").addressLine2("London")
            .postCode("SE1 234").country("UK").build();
        assertNotNull(addressMapper.mapAddress(address).toString());

    }

    @Test
    public void testAddressMapperWithNullValues() {
        Address address = Address.builder().build();
        assertNotNull(addressMapper.mapAddress(address).toString());

    }


}
