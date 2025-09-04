package uk.gov.hmcts.reform.prl.models.dto.judicial;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;


@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalisationJudgeDetails {


    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;
}
