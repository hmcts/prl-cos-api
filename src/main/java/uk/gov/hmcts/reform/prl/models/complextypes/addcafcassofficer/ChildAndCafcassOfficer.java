package uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.addcafcassofficer.CafcassOfficerPositionEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class ChildAndCafcassOfficer {

    @CCD(label = "Id")
    private final String childId;
    @CCD(label = " ")
    private final String childName;
    @CCD(label = "Full Name")
    private final String cafcassOfficerName;
    @CCD(
            label = "Position in the case",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CafcassOfficerPositionEnum"
    )
    private final CafcassOfficerPositionEnum cafcassOfficerPosition;
    @CCD(label = "Other (if position is not selected)")
    private final String cafcassOfficerOtherPosition;
    @CCD(label = "Email address")
    private final String cafcassOfficerEmailAddress;
    @CCD(label = "Phone number")
    private final String cafcassOfficerPhoneNo;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Cafcass or Cafcass Cymru officer details", typeOverride = FieldType.Label)
  private String cafcassOfficerLabel;
  // ==== end synthesised definition-only fields ====
}
