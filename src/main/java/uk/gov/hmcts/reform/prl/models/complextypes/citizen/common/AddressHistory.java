package uk.gov.hmcts.reform.prl.models.complextypes.citizen.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AddressHistory {
    private final YesOrNo isAtAddressLessThan5Years;
    private List<Element<Address>> addressHistory;
}
