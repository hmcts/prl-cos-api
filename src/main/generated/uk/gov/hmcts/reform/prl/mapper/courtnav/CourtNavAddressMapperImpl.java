package uk.gov.hmcts.reform.prl.mapper.courtnav;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-21T11:01:27+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Amazon.com Inc.)"
)
@Component
public class CourtNavAddressMapperImpl implements CourtNavAddressMapper {

    @Override
    public Address map(CourtNavAddress courtNavAddress) {
        if ( courtNavAddress == null ) {
            return null;
        }

        Address.AddressBuilder address = Address.builder();

        address.addressLine1( courtNavAddress.getAddressLine1() );
        address.addressLine2( courtNavAddress.getAddressLine2() );
        address.addressLine3( courtNavAddress.getAddressLine3() );
        address.postTown( courtNavAddress.getPostTown() );
        address.county( courtNavAddress.getCounty() );
        address.country( courtNavAddress.getCountry() );
        address.postCode( courtNavAddress.getPostCode() );

        return address.build();
    }
}
