package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChildDetails;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class ChildDetails {

    @CCD(label = "*First name(s)", searchable = false)
    private final String firstName;
    @CCD(label = "*Last name", searchable = false)
    private final String lastName;
    @CCD(label = "*Date of birth", hint = "For example, 12 11 2007", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = "*Gender", searchable = false, typeOverride = FieldType.Text)
    private final Gender gender;
    @CCD(label = "*Child's gender", searchable = false)
    private final String otherGender;
    @CCD(label = "*Order applied for", searchable = false)
    private final String orderAppliedFor;
    @CCD(label = "*What is the applicant's relationship to child?", searchable = false)
    private final String applicantsRelationshipToChild;
    @CCD(
            label = "*Describe the applicant relationship to the child",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String otherApplicantsRelationshipToChild;
    @CCD(label = "*What is the respondent's relationship to child?", searchable = false)
    private final String respondentsRelationshipToChild;
    @CCD(
            label = "*Describe the respondent relationship to the child",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String otherRespondentsRelationshipToChild;
    @CCD(label = "*Who does the child live with?", searchable = false)
    private final String childLiveWith;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherPersonLivingWithChild"
    )
    private List<Element<OtherPersonWhoLivesWithChildDetails>> personWhoLivesWithChild;
    @CCD(
            label = "*State who has parental responsibility for the child and how they have parental responsibility (e.g., 'child's mother', 'child's father and was married to the mother when child was born')",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String parentalResponsibilityDetails;

    @CCD(
            label = "Cafcass officer added",
            showCondition = "cafcassOfficerAdded= \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo cafcassOfficerAdded;
    @CCD(label = "Name of CAFCASS(Cymru) OFFICER", searchable = false)
    private final String cafcassOfficerName;
    @CCD(label = "Email Address", searchable = false)
    private final String cafcassOfficerEmailAddress;
    @CCD(label = "Telephone number", searchable = false)
    private final String cafcassOfficerPhoneNo;
    @CCD(label = "Resolution Reason", searchable = false)
    private final String finalDecisionResolutionReason;
    @CCD(label = "Resolution Date", searchable = false)
    private final String finalDecisionResolutionDate;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "### Cafcass officer",
          showCondition = "cafcassOfficerAdded=\"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String cafcassOfficerLabel;
  // ==== end synthesised definition-only fields ====
}
