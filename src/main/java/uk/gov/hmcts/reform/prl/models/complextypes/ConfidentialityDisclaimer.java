package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityChecksDisclaimerEnum;

import java.util.List;

@Data
@Builder
public class ConfidentialityDisclaimer {

    @JsonProperty("confidentialityChecksChecked")
    private final List<ConfidentialityChecksDisclaimerEnum> confidentialityChecksChecked;

    @JsonCreator
    public ConfidentialityDisclaimer(List<ConfidentialityChecksDisclaimerEnum> confidentialityChecksChecked) {
        this.confidentialityChecksChecked  = confidentialityChecksChecked;
    }

}
