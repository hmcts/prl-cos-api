package uk.gov.hmcts.reform.prl.services.solicitornotifications;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public interface SolicitorNotificationService {


    public void generateAndSendNotificationToApplicantSolicitor(CaseDetails caseDetails) throws Exception;

    public void generateAndSendNotificationToRespondentSolicitor(CaseDetails caseDetails) throws Exception;

    public void generateAndSendNotificationToRespondent(CaseDetails caseDetails) throws Exception;
}
