package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class MiamExemptions {

    private String reasonsForMiamExemption;
    private String domesticViolenceEvidence;
    private String urgencyEvidence;
    private String childProtectionEvidence;
    private String previousAttendenceEvidence;
    private String otherGroundsEvidence;

}
