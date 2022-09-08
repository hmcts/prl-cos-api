package uk.gov.hmcts.reform.prl.services.solicitornotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorNotificationServiceImpl implements SolicitorNotificationService {

    @Autowired
    private final SolicitorNotificationEmailService solicitorNotificationEmailService;

    @Autowired
    private final ObjectMapper objectMapper;

    @Override
    public CaseData generateAndSendNotificationToApplicantSolicitor(CaseDetails caseDetails) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (caseData.getState() != State.CASE_WITHDRAWN) {
            solicitorNotificationEmailService.sendC100ApplicantSolicitorNotification(caseDetails);
        }

        return caseData;
    }

    @Override
    public CaseData generateAndSendNotificationToRespondentSolicitor(CaseDetails caseDetails) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (caseData.getState() == State.ALL_FINAL_ORDERS_ISSUED) {
            solicitorNotificationEmailService.sendC100RespondentSolicitorNotification(caseDetails);
        }

        return caseData;
    }

    @Override
    public CaseData generateAndSendNotificationToRespondent(CaseDetails caseDetails) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (caseData.getState() == State.ALL_FINAL_ORDERS_ISSUED) {
            solicitorNotificationEmailService.sendC100RespondentNotification(caseDetails);
        }

        return caseData;
    }
}
