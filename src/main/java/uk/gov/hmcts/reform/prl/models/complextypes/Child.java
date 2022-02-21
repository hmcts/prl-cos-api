package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class Child {

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown; //TODO: field not used
    private final Gender gender;
    private final String otherGender;
    private final List<OrderTypeEnum> orderAppliedFor;
    private final RelationshipsEnum applicantsRelationshipToChild;
    private final String otherApplicantsRelationshipToChild;
    private final RelationshipsEnum  respondentsRelationshipToChild;
    private final String otherRespondentsRelationshipToChild;
    private final Address address;
    private final YesOrNo isChildAddressConfidential;
    private final List<LiveWithEnum> childLiveWith;
    private final List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild;
    private final String parentalResponsibilityDetails;


}
