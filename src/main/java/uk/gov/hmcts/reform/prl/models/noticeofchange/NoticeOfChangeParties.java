package uk.gov.hmcts.reform.prl.models.noticeofchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeOfChangeParties {
    @CCD(label = " ")
    private final String firstName;
    @CCD(label = " ")
    private final String lastName;
}
