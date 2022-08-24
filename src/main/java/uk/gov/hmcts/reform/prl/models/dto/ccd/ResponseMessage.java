package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResponseMessage {
    private String message;
}
