package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CaseNoteDetails {
    private final String subject;
    private final String caseNote;
}
