package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;


@Getter
@Setter
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class CaseWorkerNotificationEmailEvent {
    private final CaseDetails caseDetails;
    private final String typeOfEvent;
    private final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsModel;
    private final UserDetails userDetails;
    private final String courtEmailAddress;
}
