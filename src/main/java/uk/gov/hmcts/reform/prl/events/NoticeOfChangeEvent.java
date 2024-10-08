package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


@Getter
@Setter
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class NoticeOfChangeEvent {
    private final CaseData caseData;
    private final String solicitorEmailAddress;
    private final String solicitorName;
    private final int representedPartyIndex;
    private final SolicitorRole.Representing representing;
    private final String typeOfEvent;
    //As part of clean up accessCode field usage was removed
    private final String accessCode;
}
