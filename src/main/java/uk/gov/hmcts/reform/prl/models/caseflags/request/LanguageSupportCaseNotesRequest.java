package uk.gov.hmcts.reform.prl.models.caseflags.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LanguageSupportCaseNotesRequest {
    private final String languageSupportNotes;
    private String partyIdamId;
}
