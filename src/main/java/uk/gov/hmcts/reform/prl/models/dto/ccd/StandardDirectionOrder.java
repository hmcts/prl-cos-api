package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.RespondentOccupationEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.*;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandardDirectionOrder {

    @JsonProperty("sdoPreamblesList")
    private final List<SDOPreamblesEnum> sdoPreamblesList;
    @JsonProperty("sdoHearingsAndNextStepsList")
    private final List<SDOHearingsAndNextStepsEnum> sdoHearingsAndNextStepsList;
    @JsonProperty("sdoCafcassOrCymruList")
    private final List<SDOCafcassOrCymruEnum> sdoCafcassOrCymruList;
    @JsonProperty("sdoLocalAuthorityList")
    private final List<SDOLocalAuthorityEnum> sdoLocalAuthorityList;
    @JsonProperty("sdoCourtList")
    private final List<SDOCourtEnum> sdoCourtList;
    @JsonProperty("sdoDocumentationAndEvidenceList")
    private final List<SDODocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceList;
    @JsonProperty("sdoOtherList")
    private final List<SDOOtherEnum> sdoOtherList;

}
