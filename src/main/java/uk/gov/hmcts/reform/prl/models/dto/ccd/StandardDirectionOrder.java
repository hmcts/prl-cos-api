package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandardDirectionOrder {

    @JsonProperty("sdoPreamblesList")
    private final List<SdoPreamblesEnum> sdoPreamblesList;
    @JsonProperty("sdoHearingsAndNextStepsList")
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsList;
    @JsonProperty("sdoCafcassOrCymruList")
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruList;
    @JsonProperty("sdoLocalAuthorityList")
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityList;
    @JsonProperty("sdoCourtList")
    private final List<SdoCourtEnum> sdoCourtList;
    @JsonProperty("sdoDocumentationAndEvidenceList")
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceList;
    @JsonProperty("sdoOtherList")
    private final List<SdoOtherEnum> sdoOtherList;

}
