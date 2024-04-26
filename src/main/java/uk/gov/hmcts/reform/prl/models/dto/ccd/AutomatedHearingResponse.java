package uk.gov.hmcts.reform.prl.models.dto.ccd;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomatedHearingResponse {

    @Size(max = 30)
    private String hearingRequestID;

    @Size(max = 100)
    private String status;

    private DateTime timeStamp;

    private Number versionNumber;

}
