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
public class Landlord {
    @CCD(label = "*Who is named on the rental agreement? Please specify all that apply", searchable = false)
    private final List<MortgageNamedAfterEnum> mortgageNamedAfterList;
    @CCD(
            label = " ",
            hint = "Provide the details in the box below",
            showCondition = "mortgageNamedAfterList CONTAINS \"someoneElse\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String textAreaSomethingElse;
    @CCD(label = "*What is the name of the landlord of the rented property?", searchable = false)
    private final String landlordName;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
}
