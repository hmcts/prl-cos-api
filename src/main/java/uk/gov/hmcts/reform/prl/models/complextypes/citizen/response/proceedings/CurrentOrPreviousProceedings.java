package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CurrentOrPreviousProceedings {
    private final YesOrNo haveChildrenBeenInvolvedInCourtCase;
    private final YesOrNo courtOrderMadeForProtection;
    @JsonProperty("proceedingsList")
    private List<Element<Proceedings>> proceedingsList;
}
