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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Child {

    private final String firstName;
    private final String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown; //TODO: field not used
    private final Gender gender;
    private final String otherGender;
    private final List<OrderTypeEnum> orderAppliedFor;
    private final RelationshipsEnum applicantsRelationshipToChild;
    private final String otherApplicantsRelationshipToChild;
    private final RelationshipsEnum  respondentsRelationshipToChild;
    private final String otherRespondentsRelationshipToChild;
    @JsonIgnore
    private final Address address;
    @JsonIgnore
    private final YesOrNo isChildAddressConfidential;
    private final List<LiveWithEnum> childLiveWith;
    private final List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild;
    private final DynamicList whoDoesTheChildLiveWith;
    private final String parentalResponsibilityDetails;
    private final YesOrNo isFinalOrderIssued;

    public boolean hasConfidentialInfo() {
        return YesOrNo.Yes.equals(this.isChildAddressConfidential);
    }

    private final String cafcassOfficerName;
    private final CafcassOfficerPositionEnum cafcassOfficerPosition;
    private final String cafcassOfficerOtherPosition;
    private final String cafcassOfficerEmailAddress;
    private final String cafcassOfficerPhoneNo;

}
