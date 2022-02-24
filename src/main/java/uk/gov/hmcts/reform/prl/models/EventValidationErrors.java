package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Event;

import java.util.List;

@Data
@Builder
public class EventValidationErrors {

    private final Event event;
    private final List<String> errors;
    private final List<String> nestedErrors;

}
