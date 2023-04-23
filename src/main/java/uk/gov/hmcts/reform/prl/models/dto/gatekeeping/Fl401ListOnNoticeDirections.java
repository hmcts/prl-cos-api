package uk.gov.hmcts.reform.prl.models.dto.gatekeeping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.FL401ListOnNoticeDirectionsEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class Fl401ListOnNoticeDirections {

    private List<FL401ListOnNoticeDirectionsEnum> additionalDirections;
    private String reducedNoticePeriodDetails;
    private DynamicList linkedCaCasesList;
    private String linkedCaCasesFurtherDetails;
    private String applicantNeedsFurtherInfoDetails;
    private String respondentNeedsFileStatementDetails;

}
