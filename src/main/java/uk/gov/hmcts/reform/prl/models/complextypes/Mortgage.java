package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.models.Address;

import java.util.List;

@Data
@Builder
public class Mortgage {
    private final List<MortgageNamedAfterEnum> mortgageNamedAfter;
    private final String textAreaSomethingElse;
    private final String mortgageNumber;
    private final String mortgageLenderName;
    private final Address address;
}
