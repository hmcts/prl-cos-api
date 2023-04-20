package uk.gov.hmcts.reform.prl.models.dto.gatekeeping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.FL401ListOnNoticeDirectionsEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class Fl401ListOnNoticeDirections {

    private final FL401ListOnNoticeDirectionsEnum additionalDirections;
    private final String reducedNoticePeriodDetails;
    private final DynamicList linkedCaCasesList;
    private final String linkedCaCasesFurtherDetails;
    private final String applicantNeedsFurtherInfoDetails;
    private final String respondentNeedsFileStatementDetails;

}
