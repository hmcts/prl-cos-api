package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.addcafcassofficer.CafcassOfficerPositionEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class Child {

    @CCD(label = "*First name(s)", searchable = false)
    private final String firstName;
    @CCD(label = "*Last name", searchable = false)
    private final String lastName;
    @CCD(label = "*Date of birth", hint = "For example, 12 11 2007", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = " ", searchable = false)
    private final DontKnow isDateOfBirthUnknown; //TODO: field not used
    @CCD(label = "*Gender", searchable = false)
    private final Gender gender;
    @CCD(label = "*Child's gender", searchable = false)
    private final String otherGender;
    @CCD(
            label = "*Order applied for",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "OrderAppliedFor"
    )
    private final List<OrderTypeEnum> orderAppliedFor;
    @CCD(label = "*What is the applicant's relationship to child?", searchable = false)
    private final RelationshipsEnum applicantsRelationshipToChild;
    @CCD(
            label = "*Describe the applicant relationship to the child",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String otherApplicantsRelationshipToChild;
    @CCD(label = "*What is the respondent's relationship to child?", searchable = false)
    private final RelationshipsEnum  respondentsRelationshipToChild;
    @CCD(
            label = "*Describe the respondent relationship to the child",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String otherRespondentsRelationshipToChild;
    @JsonIgnore
    private final Address address;
    @JsonIgnore
    private final YesOrNo isChildAddressConfidential;
    @CCD(label = "*Who does the child live with?", searchable = false)
    private final List<LiveWithEnum> childLiveWith;
    @CCD(label = "Person", searchable = false)
    private final List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild;
    @CCD(
            label = "*State who has parental responsibility for the child and how they have parental responsibility (e.g., 'child's mother', 'child's father and was married to the mother when child was born')",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String parentalResponsibilityDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isFinalOrderIssued;

    public boolean hasConfidentialInfo() {
        return YesOrNo.Yes.equals(this.isChildAddressConfidential);
    }

    @CCD(label = "Name of CAFCASS(Cymru) OFFICER", searchable = false)
    private final String cafcassOfficerName;
    @CCD(
            label = "Position in the case",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CafcassOfficerPositionEnum"
    )
    private final CafcassOfficerPositionEnum cafcassOfficerPosition;
    @CCD(label = "Other (if position is not selected)", searchable = false)
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
  @CCD(label = "**Parental responsibility**", searchable = false, typeOverride = FieldType.Label)
  private String parentalResponsibility;
  @CCD(label = "### Add new child", searchable = false, typeOverride = FieldType.Label)
  private String addNewChildLabel;
  @CCD(label = "Relationship to Applicant(s)", searchable = false)
  private String relationshipToApplicant;
  @CCD(label = "Relationship to Respondent(s)", searchable = false)
  private String relationshipToRespondent;
  // ==== end synthesised definition-only fields ====
}
