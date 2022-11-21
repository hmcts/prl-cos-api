package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildPassportDetails {
    private List<AbductionChildPassportPossessionEnum> childPassportHolder;
    private YesOrNo childrenHasMoreThanOnePassPort;
    private String abductionChildPassportPosessionOtherDetail;
}
