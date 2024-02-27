package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildPassportDetails {

    @JsonProperty("newChildPassportPossession")
    private List<NewPassportPossessionEnum> newChildPassportPossession;
    private YesOrNo newChildHasMultiplePassports;
    private String newChildPassportPossessionOtherDetails;
}
