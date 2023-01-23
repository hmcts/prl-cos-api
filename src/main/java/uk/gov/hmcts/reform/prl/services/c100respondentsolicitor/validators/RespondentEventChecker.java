package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public interface RespondentEventChecker {

    boolean isStarted(CaseData caseData);

    boolean hasMandatoryCompleted(CaseData caseData);

}
