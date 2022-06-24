package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Address;

import static org.junit.Assert.assertNotNull;


@RunWith(MockitoJUnitRunner.class)
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
