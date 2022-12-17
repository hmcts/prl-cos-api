package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Address;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AddressCollection {
    private final Address pastAddress;
}
