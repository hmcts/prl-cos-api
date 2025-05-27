package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Builder
public record WorkAllocationTaskStatusEvent(CallbackRequest callbackRequest, String authorisation) {
}
