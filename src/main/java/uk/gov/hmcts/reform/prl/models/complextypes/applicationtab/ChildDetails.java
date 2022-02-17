package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChildDetails;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ChildDetails {

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown;
    private final Gender gender;
    private final String otherGender;
    private final String orderAppliedFor;
    private final String applicantsRelationshipToChild;
    private final String otherApplicantsRelationshipToChild;
    private final String respondentsRelationshipToChild;
    private final String otherRespondentsRelationshipToChild;
    private final String childLiveWith;
    private final YesNoDontKnow childrenKnownToLocalAuthority;
    private final String childrenKnownToLocalAuthorityTextArea;
    private final YesNoDontKnow childrenSubjectOfChildProtectionPlan;
    private List<Element<OtherPersonWhoLivesWithChildDetails>> personWhoLivesWithChild;
    private final Address address;
    private final YesOrNo isChildAddressConfidential;
    private final YesOrNo childUnsupervisedTime;
    private final YesOrNo childContactFromOtherRecipients;
    private final String relationshipToApplicant;
    private final String relationshipToRespondent;
    private final String parentalResponsibilityDetails;

}
