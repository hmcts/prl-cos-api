package uk.gov.hmcts.reform.prl.models.dto.ccd.closingcases;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.closingcase.CaseClosingReasonForChildren;
import uk.gov.hmcts.reform.prl.models.complextypes.closingcase.DateFinalDecisionWasMade;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClosingCaseOptions {

    @JsonProperty("isTheDecisionAboutAllChildren")
    private final YesOrNo isTheDecisionAboutAllChildren;
    @JsonProperty("childOptionsForFinalDecision")
    private final DynamicMultiSelectList childOptionsForFinalDecision;
    @JsonProperty("finalOutcomeForChildren")
    private final List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren;
    @JsonProperty("dateFinalDecisionWasMade")
    private DateFinalDecisionWasMade dateFinalDecisionWasMade;
}
