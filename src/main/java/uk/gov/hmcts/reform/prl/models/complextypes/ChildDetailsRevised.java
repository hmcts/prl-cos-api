package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.addcafcassofficer.CafcassOfficerPositionEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class ChildDetailsRevised {

    private final String firstName;
    private final String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown; //TODO: field not used
    private final Gender gender;
    private final String otherGender;
    private final List<OrderTypeEnum> orderAppliedFor;
    private final String parentalResponsibilityDetails;
    private final DynamicList whoDoesTheChildLiveWith;

    private final YesOrNo isFinalOrderIssued;

    private final String cafcassOfficerName;
    private final CafcassOfficerPositionEnum cafcassOfficerPosition;
    private final String cafcassOfficerOtherPosition;
    private final String cafcassOfficerEmailAddress;
    private final String cafcassOfficerPhoneNo;


}
