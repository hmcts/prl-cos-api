package uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLog;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogMessage;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridMessageResponse;
import uk.gov.hmcts.reform.prl.models.sendgrid.logs.MessageFailureView;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageFailureMapper {

    private final CcdCoreCaseDataService caseDataService;
    private final SystemUserService systemUserService;

    public List<MessageFailureView> convertToEmailFailureView(List<SendGridLogMessage> logMessages) {
        return logMessages.stream()
            .map(this::convertToEmailFailureView)
            .toList();
    }

    private MessageFailureView convertToEmailFailureView(SendGridLogMessage logMessage) {
        SendGridLog sendGridLog = logMessage.getSendGridLog();
        SendGridMessageResponse messageResponse = logMessage.getSendGridMessageResponse();
        CaseDetails caseDetails = getCaseDetails(messageResponse);
        String caseReference = getCaseReference(caseDetails);
        String courtName = getCourtName(caseDetails);
        String createdAt = sendGridLog.getSgMessageIdCreatedAt();

        return MessageFailureView.builder()
            .caseReference(caseReference)
            .courtName(courtName)
            .sentDate(OffsetDateTime.parse(createdAt))
            .subject(sendGridLog.getSubject())
            .status(sendGridLog.getStatus())
            .reason(sendGridLog.getReason())
            .toEmailAddress(sendGridLog.getToEmail())
            .messageId(sendGridLog.getSgMessageId())
            .templateId(messageResponse.getTemplateId())
            .templateName(messageResponse.getCustomArgs().getSgTemplateName())
            .build();
    }

    private CaseDetails getCaseDetails(SendGridMessageResponse messageResponse) {
        String caseReference = messageResponse.getCustomArgs().getCaseReference();
        if (StringUtils.isBlank(caseReference)) {
            log.warn("Case reference is missing in SendGrid message: {}", messageResponse.getSgMessageId());
            return null;
        }

        String authToken = systemUserService.getSysUserToken();

        try {
            return caseDataService.findCaseById(authToken, caseReference);
        }  catch (Exception e) {
            log.error("Error while trying to find case by reference {}", caseReference, e);
            return null;
        }
    }

    private String getCaseReference(CaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails)
            .map(CaseDetails::getId)
            .map(String::valueOf)
            .orElse(null);
    }

    private String getCourtName(CaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails)
            .map(CaseDetails::getData)
            .map(data -> data.get(PrlAppsConstants.COURT_NAME))
            .map(String::valueOf)
            .orElse(null);
    }
}
