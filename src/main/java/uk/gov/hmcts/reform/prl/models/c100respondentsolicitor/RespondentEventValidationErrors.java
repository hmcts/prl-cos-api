package uk.gov.hmcts.reform.prl.models.c100respondentsolicitor;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;

import java.util.List;

@Data
@Builder
public class RespondentEventValidationErrors {

    private final RespondentSolicitorEvents event;
    private final List<String> errors;
    private final List<String> nestedErrors;

}
