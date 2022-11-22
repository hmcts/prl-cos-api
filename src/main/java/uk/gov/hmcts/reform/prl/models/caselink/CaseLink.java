package uk.gov.hmcts.reform.prl.models.caselink;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CaseLink {
    private final String CaseReference;
    private final LinkReason ReasonForLink;
    private final DateTime CreatedDateTime;
    private final String CaseType;
}
