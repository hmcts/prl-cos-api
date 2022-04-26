package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.CaseTransferOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ReasonForTransferEnum;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageOrders {

    private final List<String> cafcassEmailAddress;
    private final List<String> otherEmailAddress;
    private CaseTransferOptionsEnum caseTransferOptions;
    @JsonProperty("reasonsForTransfer")
    private List<ReasonForTransferEnum> reasonForTransfer;
}
