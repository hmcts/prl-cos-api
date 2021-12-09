package uk.gov.hmcts.reform.prl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Getter
@RequiredArgsConstructor
public class CaseDataChanged {

    private final CaseData caseData;

}
