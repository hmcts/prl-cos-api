package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Address {

    //TODO: need to ensure this can handle null values - see the civil implementation

    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String postTown;
    private final String county;
    private final String country;
    private final String postCode;


}
