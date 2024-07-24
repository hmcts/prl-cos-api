package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


@Getter
@Setter
@RequiredArgsConstructor
@Builder
public class TransferToAnotherCourtEvent {
    private final CaseData caseData;
    private final String typeOfEvent;
    private final String authorisation;
}
