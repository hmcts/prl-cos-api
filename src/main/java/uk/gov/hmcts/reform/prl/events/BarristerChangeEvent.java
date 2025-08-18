package uk.gov.hmcts.reform.prl.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.barrister.TypeOfBarristerEventEnum;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


@Getter
@Setter
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class BarristerChangeEvent {
    private final CaseData caseData;
    private final TypeOfBarristerEventEnum typeOfEvent;
    private final AllocatedBarrister allocatedBarrister;
}
