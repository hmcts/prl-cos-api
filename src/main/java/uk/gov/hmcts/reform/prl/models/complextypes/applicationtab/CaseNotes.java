package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CaseNotes {
    private final String subject;
    private final String caseNote;
}

