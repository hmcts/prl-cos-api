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

@Data
@Builder
public class ChildDetails {

    private final String firstName;
    private final String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private final String otherGender;
    private final String orderAppliedFor;
    private final String applicantsRelationshipToChild;
    private final String otherApplicantsRelationshipToChild;
    private final String respondentsRelationshipToChild;
    private final String otherRespondentsRelationshipToChild;
    private final String childLiveWith;
    private List<Element<OtherPersonWhoLivesWithChildDetails>> personWhoLivesWithChild;
    private final String parentalResponsibilityDetails;

    private final YesOrNo cafcassOfficerAdded;
    private final String cafcassOfficerName;
    private final String cafcassOfficerEmailAddress;
    private final String cafcassOfficerPhoneNo;

}
