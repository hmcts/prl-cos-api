package uk.gov.hmcts.reform.prl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class NoticeOfChangeEventHandlerTest {

    @Mock
    private NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateVars emailTemplateVars;

    @InjectMocks
    private NoticeOfChangeEventHandler noticeOfChangeEventHandler;

    @Test
    public void shouldNotifyLegalRepresentative() {
        log.info("My changes");
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        final String solicitorEmailAddress = "test solicitor email";
        final String solicitorName = "test solicitor name";
        final int representedPartyIndex = 1;
        final SolicitorRole.Representing representing = CAAPPLICANT;
        final NoticeOfChangeEvent noticeOfChangeEvent = NoticeOfChangeEvent.builder()
            .caseData(caseData).solicitorEmailAddress(solicitorEmailAddress)
            .solicitorName(solicitorName)
            .representedPartyIndex(representedPartyIndex)
            .representing(representing)
            .build();

        when(noticeOfChangeContentProvider.buildNoticeOfChangeEmail(caseData,"test Solicitor Name")).thenReturn(emailTemplateVars);

        noticeOfChangeEventHandler.notifyLegalRepresentative(noticeOfChangeEvent);

    }

}
