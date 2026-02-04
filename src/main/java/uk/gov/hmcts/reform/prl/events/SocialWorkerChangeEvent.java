package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.localauthority.TypeOfSocialWorkerEventEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Getter
@Setter
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class SocialWorkerChangeEvent {
    private final CaseData caseData;
    private final TypeOfSocialWorkerEventEnum typeOfEvent;
}
