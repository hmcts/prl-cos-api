package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CaseNoteDetails {
    private final String subject;
    private final String caseNote;
    private final String user;
    private final String dateAdded;
    private final LocalDateTime dateCreated;
}
