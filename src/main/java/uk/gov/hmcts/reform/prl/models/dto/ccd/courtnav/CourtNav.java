package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourtNav {
    @JsonProperty("applicantAge")
    private final ApplicantAge applicantAge;
    private final String specialCourtName;
    private YesOrNo courtNavApproved;
    private YesOrNo hasDraftOrder;
    private String caseOrigin;
    private String numberOfAttachments;

}
