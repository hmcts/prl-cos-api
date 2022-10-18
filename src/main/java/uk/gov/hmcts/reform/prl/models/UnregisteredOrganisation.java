package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class UnregisteredOrganisation {
    private String name;
    private Address address;
}
