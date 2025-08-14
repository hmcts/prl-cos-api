package uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.addcafcassofficer.CafcassOfficerPositionEnum;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class ChildAndCafcassOfficer {

    private final String childId;
    private final String childName;
    private final String cafcassOfficerName;
    private final CafcassOfficerPositionEnum cafcassOfficerPosition;
    private final String cafcassOfficerOtherPosition;
    private final String cafcassOfficerEmailAddress;
    private final String cafcassOfficerPhoneNo;
}
