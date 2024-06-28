package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Builder
public record CaseFlagsEvent(CallbackRequest callbackRequest, String authorisation) {
}
