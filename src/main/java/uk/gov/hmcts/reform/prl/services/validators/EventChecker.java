package uk.gov.hmcts.reform.prl.services.validators;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public interface EventChecker {

    boolean isFinished(CaseData caseData);

    boolean isStarted(CaseData caseData);

    boolean hasMandatoryCompleted(CaseData caseData);

}
