package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
public class ManageOrderNotificationsEvent {
    private final CaseDetails caseDetails;
    private final String typeOfEvent;
}
