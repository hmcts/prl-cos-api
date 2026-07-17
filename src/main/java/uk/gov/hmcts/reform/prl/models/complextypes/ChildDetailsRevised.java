package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.addcafcassofficer.CafcassOfficerPositionEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class ChildDetailsRevised {

    @CCD(label = "*First name(s)")
    private final String firstName;
    @CCD(label = "*Last name")
    private final String lastName;
    @CCD(label = "*Date of birth", hint = "For example, 12 11 2007")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(ignore = true)
    private final DontKnow isDateOfBirthUnknown; //TODO: field not used
    @CCD(label = "*Gender")
    private final Gender gender;
    @CCD(label = "*Child's gender")
    private final String otherGender;
    @CCD(label = "*Order applied for", typeOverride = FieldType.Text, typeParameterOverride = "OrderAppliedFor")
    private final List<OrderTypeEnum> orderAppliedFor;
    @CCD(
            label = "*State who has parental responsibility for the child and how they have parental responsibility (e.g., 'child's mother', 'child's father and was married to the mother when child was born')",
            typeOverride = FieldType.TextArea
    )
    private final String parentalResponsibilityDetails;
    @CCD(ignore = true)
    private final DynamicList whoDoesTheChildLiveWith;

    @CCD(ignore = true)
    private final YesOrNo isFinalOrderIssued;

    @CCD(label = "Name of CAFCASS(Cymru) OFFICER", searchable = false)
    private final String cafcassOfficerName;
    @CCD(ignore = true)
    private final CafcassOfficerPositionEnum cafcassOfficerPosition;
    @CCD(ignore = true)
    private final String cafcassOfficerOtherPosition;
    @CCD(label = "Email Address", searchable = false)
    private final String cafcassOfficerEmailAddress;
    @CCD(label = "Telephone number", searchable = false)
    private final String cafcassOfficerPhoneNo;

    @CCD(label = "Resolution Reason", searchable = false)
    private final String finalDecisionResolutionReason;
    @CCD(label = "Resolution Date", searchable = false)
    private final String finalDecisionResolutionDate;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "**Parental responsibility**", typeOverride = FieldType.Label)
  private String parentalResponsibility;
  @CCD(label = "Cafcass officer added", showCondition = "cafcassOfficerAdded= \"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo cafcassOfficerAdded;
  @CCD(
          label = "### Cafcass officer",
          showCondition = "cafcassOfficerAdded=\"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String cafcassOfficerLabel;
  // ==== end synthesised definition-only fields ====
}
