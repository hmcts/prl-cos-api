package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "automatedHearingAttendHearingWith")
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomatedHearingAttendHearing {

    private Boolean isWelshNeeded;
    private Boolean isInterpreterNeeded;
}
