package uk.gov.hmcts.reform.prl.services.noc;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class ChangeOfRepresentation {
    String respondent;
    String child;
    LocalDate date;
    String by;
    String via;

    ChangedRepresentative removed;
    ChangedRepresentative added;
}
