package uk.gov.hmcts.reform.prl.models.complextypes.citizen.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AddressHistory {
    @CCD(
            label = "*Has the respondent lived at this address for more than 5 years?\n ",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isAtAddressLessThan5Years;
    @CCD(
            label = " ",
            showCondition = "isAtAddressLessThan5Years=\"No\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AddressUK"
    )
    private List<Element<Address>> previousAddressHistory;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Give details of previous addresses you have lived at in the last 5 years, starting with your most recent address",
          showCondition = "isAtAddressLessThan5Years=\"No\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String detailsOfAddressHistoryLabel;
  // ==== end synthesised definition-only fields ====
}
