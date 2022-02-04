package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class AttendingTheHearing {

    private final YesOrNo isWelshNeeded;
    private final YesOrNo isInterpreterNeeded;
    private final YesOrNo isDisabilityPresent;
    private final String adjustmentsRequired;
    private final YesOrNo isSpecialArrangementsRequired;
    private final String specialArrangementsRequired;
    private final YesOrNo isIntermediaryNeeded;
    private final String reasonsForIntermediary;

}
