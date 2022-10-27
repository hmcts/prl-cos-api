package uk.gov.hmcts.reform.prl.services.noc;

import uk.gov.hmcts.reform.prl.enums.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.RespondentSolicitor;
import uk.gov.hmcts.reform.prl.models.WithSolicitor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public interface NoticeOfChangeUpdateAction {
    boolean accepts(SolicitorRole.Representing representing);

    Map<String, Object> applyUpdates(WithSolicitor solicitorContainer, CaseData caseData,
                                     RespondentSolicitor solicitor);
}
