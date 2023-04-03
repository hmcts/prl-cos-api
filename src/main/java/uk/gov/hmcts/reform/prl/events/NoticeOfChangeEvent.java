package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


@Data
@Getter
@RequiredArgsConstructor
@Builder
public class NoticeOfChangeEvent {
    private final CaseData caseData;
    private final String solicitorEmailAddress;
    private final String solicitorName;
    private final int representedPartyIndex;
    private final SolicitorRole.Representing representing;
}
