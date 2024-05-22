package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RespPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespChildPassportDetails {

    @JsonProperty("respChildPassportPossession")
    private List<RespPassportPossessionEnum> respChildPassportPossession;
    private YesOrNo respChildHasMultiplePassports;
    private String respChildPassportPossessionOtherDetails;
}
