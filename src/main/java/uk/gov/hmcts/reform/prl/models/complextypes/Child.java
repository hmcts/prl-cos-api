package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class Child {

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown;
    private final Gender gender;
    private final String otherGender;
    private final List<OrderTypeEnum> orderAppliedFor;
    private final RelationshipsEnum applicantsRelationshipToChild;
    private final String otherApplicantsRelationshipToChild;
    private final RelationshipsEnum  respondentsRelationshipToChild;
    private final String otherRespondentsRelationshipToChild;
    private final List<LiveWithEnum> childLiveWith;
    private final String otherPersonWhoLivesWithChild;
    private final YesOrNo isChildCurrentAddressKnown;
    private final Address address;
    private final YesOrNo isChildAddressConfidential;
    private final YesOrNo childUnsupervisedTime;
    private final YesOrNo childContactFromOtherRecipients;
    private final String relationshipToApplicant;
    private final String relationshipToRespondent;


}
