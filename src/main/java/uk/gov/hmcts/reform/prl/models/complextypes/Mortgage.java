package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.models.Address;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class Mortgage {
    @CCD(label = "*Who is named on the mortgage (please select all that apply)?", searchable = false)
    private final List<MortgageNamedAfterEnum> mortgageNamedAfter;
    @CCD(
            label = " ",
            hint = "Provide the details in the box below",
            showCondition = "mortgageNamedAfter CONTAINS \"someoneElse\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String textAreaSomethingElse;
    @CCD(label = "Mortgage number (if known)", searchable = false)
    private final String mortgageNumber;
    @CCD(label = "*Mortgage lender’s name", searchable = false)
    private final String mortgageLenderName;
    @CCD(label = " ", hint = "Mortgage lender’s postcode", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
}
