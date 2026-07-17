package uk.gov.hmcts.reform.prl.models.dto.cafcass;
import uk.gov.hmcts.reform.prl.enums.addcafcassofficer.CafcassOfficerPositionEnum;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Child {

    @CCD(label = "*First name(s)", searchable = false)
    private String firstName;
    @CCD(label = "*Last name", searchable = false)
    private String lastName;
    @CCD(label = "*Date of birth", hint = "For example, 12 11 2007", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    @CCD(label = " ", searchable = false)
    private DontKnow isDateOfBirthUnknown; //TODO: field not used
    @CCD(label = "*Gender", searchable = false)
    private Gender gender;
    @CCD(label = "*Child's gender", searchable = false)
    private String otherGender;
    @CCD(
            label = "*Order applied for",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "OrderAppliedFor"
    )
    private List<OrderTypeEnum> orderAppliedFor;
    @CCD(label = "*What is the applicant's relationship to child?", searchable = false)
    private RelationshipsEnum applicantsRelationshipToChild;
    @CCD(
            label = "*Describe the applicant relationship to the child",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String otherApplicantsRelationshipToChild;
    @CCD(label = "*What is the respondent's relationship to child?", searchable = false)
    private RelationshipsEnum  respondentsRelationshipToChild;
    @CCD(
            label = "*Describe the respondent relationship to the child",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String otherRespondentsRelationshipToChild;
    @JsonIgnore
    private Address address;
    @JsonIgnore
    private YesOrNo isChildAddressConfidential;
    @CCD(label = "*Who does the child live with?", searchable = false)
    private List<LiveWithEnum> childLiveWith;
    @CCD(label = "Person", searchable = false)
    private List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild;
    @CCD(
            label = "*State who has parental responsibility for the child and how they have parental responsibility (e.g., 'child's mother', 'child's father and was married to the mother when child was born')",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String parentalResponsibilityDetails;
    @CCD(ignore = true)
    private WhoDoesTheChildLiveWith whoDoesTheChildLiveWith;

    public boolean hasConfidentialInfo() {
        return YesOrNo.Yes.equals(this.isChildAddressConfidential);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "**Parental responsibility**", searchable = false, typeOverride = FieldType.Label)
  private String parentalResponsibility;
  @CCD(label = "### Add new child", searchable = false, typeOverride = FieldType.Label)
  private String addNewChildLabel;
  @CCD(label = "Relationship to Applicant(s)", searchable = false)
  private String relationshipToApplicant;
  @CCD(label = "Relationship to Respondent(s)", searchable = false)
  private String relationshipToRespondent;
  @CCD(label = "Name of CAFCASS(Cymru) OFFICER", searchable = false)
  private String cafcassOfficerName;
  @CCD(
          label = "Position in the case",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "CafcassOfficerPositionEnum"
  )
  private CafcassOfficerPositionEnum cafcassOfficerPosition;
  @CCD(label = "Other (if position is not selected)", searchable = false)
  private String cafcassOfficerOtherPosition;
  @CCD(label = "Email Address", searchable = false)
  private String cafcassOfficerEmailAddress;
  @CCD(label = "Telephone number", searchable = false)
  private String cafcassOfficerPhoneNo;
  @CCD(label = " ", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isFinalOrderIssued;
  @CCD(label = "Resolution Reason", searchable = false)
  private String finalDecisionResolutionReason;
  @CCD(label = "Resolution Date", searchable = false)
  private String finalDecisionResolutionDate;
  // ==== end synthesised definition-only fields ====
}
