package uk.gov.hmcts.reform.prl.services.noc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.RespondentSolicitor;
import uk.gov.hmcts.reform.prl.models.WithSolicitor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

@Component
public class RespondentNoticeOfChangeUpdateAction implements NoticeOfChangeUpdateAction {

    private static final SolicitorRole.Representing REPRESENTING = SolicitorRole.Representing.RESPONDENT;

    @Override
    public boolean accepts(SolicitorRole.Representing representing) {
        return REPRESENTING == representing;
    }

    @Override
    public Map<String, Object> applyUpdates(WithSolicitor respondent, CaseData caseData,
                                            RespondentSolicitor solicitor) {
        respondent.setSolicitor(solicitor);
        return Map.of("respondents1", caseData.getRespondents());
    }
}
