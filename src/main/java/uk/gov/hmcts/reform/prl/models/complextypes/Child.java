package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;

@Data
@Builder
public class Child {

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown;
    private final Gender gender;
    private final String otherGender;
    private final OrderAppliedFor orderAppliedFor;
    private final RelationshipsEnum applicantsRelationshipToChild;
    private final String otherApplicantsRelationshipToChild;
    private final RelationshipsEnum  respondentsRelationshipToChild;
    private final String otherRespondentsRelationshipToChild;
    private final LiveWithEnum childLiveWith;
    private final String otherPersonWhoLivesWithChild;
    private final YesOrNo isChildCurrentAddressKnown;
    private final Address address;
    private final YesOrNo isChildAddressConfidential;
    private final String relationshipToApplicant;
    private final String relationshipToRespondent;


}
