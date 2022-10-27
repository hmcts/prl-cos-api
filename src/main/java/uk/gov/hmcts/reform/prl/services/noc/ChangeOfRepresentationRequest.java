package uk.gov.hmcts.reform.prl.services.noc;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.RespondentSolicitor;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class ChangeOfRepresentationRequest {

    List<Element<ChangeOfRepresentation>> current;
    RespondentSolicitor addedRepresentative;
    RespondentSolicitor removedRepresentative;
    ChangeOfRepresentationMethod method;
    String by;

}
