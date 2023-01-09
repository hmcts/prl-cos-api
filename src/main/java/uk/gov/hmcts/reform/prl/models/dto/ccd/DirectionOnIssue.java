package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.dio.DioCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioCourtEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectionOnIssue {

    @JsonProperty("dioPreamblesList")
    private final List<DioPreamblesEnum> dioPreamblesList;
    @JsonProperty("dioHearingsAndNextStepsList")
    private final List<DioHearingsAndNextStepsEnum> dioHearingsAndNextStepsList;
    @JsonProperty("dioCafcassOrCymruList")
    private final List<DioCafcassOrCymruEnum> dioCafcassOrCymruList;
    @JsonProperty("dioLocalAuthorityList")
    private final List<DioLocalAuthorityEnum> dioLocalAuthorityList;
    @JsonProperty("dioCourtList")
    private final List<DioCourtEnum> dioCourtList;
    @JsonProperty("dioOtherList")
    private final List<DioOtherEnum> dioOtherList;
}
