package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.models.Address;

import java.util.List;

@Data
@Builder
public class Landlord {
    private final List<MortgageNamedAfterEnum> mortgageNamedAfterList;
    private final String textAreaSomethingElse;
    private final String landlordName;
    private final Address address;
}
