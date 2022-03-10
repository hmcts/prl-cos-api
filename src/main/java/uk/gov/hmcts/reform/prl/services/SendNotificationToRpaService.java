package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendNotificationToRpaService {

    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;
    public void notifyRobotics(CaseData caseData) throws IOException {
        requireNonNull(caseData);
        sendgridService.sendEmail(c100JsonMapper.map(caseData));
    }
}
