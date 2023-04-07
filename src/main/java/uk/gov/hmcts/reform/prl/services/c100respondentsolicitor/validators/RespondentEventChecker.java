package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public interface RespondentEventChecker {

    boolean isStarted(CaseData caseData, String respondent);

    boolean isFinished(CaseData caseData, String respondent);

    default Optional<Response> findResponse(CaseData caseData, String respondent) {
        Optional<SolicitorRole> solicitorRole = SolicitorRole.from(respondent);
        if (solicitorRole.isPresent()) {
            if (caseData.getRespondents().size() > solicitorRole.get().getIndex()) {
                return ofNullable(caseData.getRespondents().get(solicitorRole.get().getIndex()).getValue().getResponse());
            }
        }
        return Optional.empty();
    }
}
