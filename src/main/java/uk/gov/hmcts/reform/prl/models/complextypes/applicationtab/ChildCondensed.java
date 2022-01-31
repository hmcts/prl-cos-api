package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
public class ChildCondensed {

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private final String otherGender;
    private final RelationshipsEnum applicantsRelationshipToChild;
    private final RelationshipsEnum  respondentsRelationshipToChild;
    private final List<LiveWithEnum> childLiveWith;
    private final List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild;
    private final String parentalResponsibilityDetails;

}
