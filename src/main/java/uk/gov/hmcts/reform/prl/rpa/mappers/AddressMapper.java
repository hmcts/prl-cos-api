package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
public class AddressMapper {

    public JsonObject mapAddress(Address address) {
        return new NullAwareJsonObjectBuilder()
            .add("AddressLine1", address.getAddressLine1())
            .add("AddressLine2", address.getAddressLine2())
            .add("AddressLine3", address.getAddressLine3())
            .add("PostTown", address.getPostTown())
            .add("County", address.getCounty())
            .add("Country", address.getCountry())
            .add("PostCode", address.getPostCode())
            .build();
    }
}
