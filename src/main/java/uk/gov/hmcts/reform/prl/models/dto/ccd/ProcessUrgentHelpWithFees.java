package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessUrgentHelpWithFees {

    private DynamicList hwfAppList;
    private String addHwfCaseNoteShort;
    private YesOrNo outstandingBalance;
    private YesOrNo managerAgreedApplicationBeforePayment;
    private YesOrNo isTheCaseInDraftState;
}
