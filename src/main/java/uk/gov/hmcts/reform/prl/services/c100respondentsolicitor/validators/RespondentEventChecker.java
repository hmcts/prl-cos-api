package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Optional;

public interface RespondentEventChecker {

    boolean isStarted(CaseData caseData, String respondent);

    boolean isFinished(CaseData caseData, String respondent);

    default Optional<Response> findResponse(CaseData caseData, String respondent) {
        Optional<Response> response = Optional.empty();;
        if (caseData.getRespondents() != null
            && !caseData.getRespondents().isEmpty()
            && respondent != null
            && !"".equalsIgnoreCase(respondent)) {
            switch (respondent) {
                case "A":
                    response = findRespondent(caseData, 0);
                    break;
                case "B":
                    response = findRespondent(caseData, 1);
                    break;
                case "C":
                    response = findRespondent(caseData, 2);
                    break;
                case "D":
                    response = findRespondent(caseData, 3);
                    break;
                case "E":
                    response = findRespondent(caseData, 4);
                    break;
                default:
                    break;
            }
        }
        return response;
    }

    private Optional<Response> findRespondent(CaseData caseData, int item) {
        if (caseData.getRespondents().size() > item) {
            return Optional.ofNullable(caseData.getRespondents().get(item).getValue().getResponse());
        }
        return Optional.empty();
    }
}
