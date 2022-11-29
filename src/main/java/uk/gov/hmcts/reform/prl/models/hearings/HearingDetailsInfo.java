package uk.gov.hmcts.reform.prl.models.hearings;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HearingDetailsInfo {

    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearingData> caseHearings;
}
