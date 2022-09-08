package uk.gov.hmcts.reform.prl.services.solicitornotifications;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public interface SolicitorNotificationService {


    public CaseData generateAndSendNotificationToApplicantSolicitor(CaseDetails caseDetails) throws Exception;

    public CaseData generateAndSendNotificationToRespondentSolicitor(CaseDetails caseDetails) throws Exception;

    public CaseData generateAndSendNotificationToRespondent(CaseDetails caseDetails) throws Exception;
}
