package uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.processing;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLog;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogMessage;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridMessageResponse;
import uk.gov.hmcts.reform.prl.models.sendgrid.logs.MessageFailureView;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageFailureMapperTest {

    @Mock
    private CcdCoreCaseDataService caseDataService;
    @Mock
    private SystemUserService systemUserService;

    @InjectMocks
    private MessageFailureMapper mapper;

    private SendGridLogMessage buildLogMessage(String caseReference) {
        SendGridLog log = SendGridLog.builder()
            .fromEmail("from@test.com")
            .sgMessageId("sg-id-123")
            .subject("Test Subject")
            .toEmail("to@test.com")
            .reason("Bounce")
            .status("FAILED")
            .sgMessageIdCreatedAt("2024-04-30T10:15:30+01:00")
            .build();

        SendGridMessageResponse.CustomArgs customArgs = SendGridMessageResponse.CustomArgs.builder()
            .caseReference(caseReference)
            .sgTemplateId("tmpl-1")
            .sgTemplateName("TemplateName")
            .build();

        SendGridMessageResponse response = SendGridMessageResponse.builder()
            .sgMessageId("sg-id-123")
            .templateId("tmpl-1")
            .customArgs(customArgs)
            .build();

        return SendGridLogMessage.builder()
            .sendGridLog(log)
            .sendGridMessageResponse(response)
            .build();
    }

    @Test
    void testConvertToEmailFailureView_MapsAllFields() {
        String caseReference = "1234567890123456";
        String courtName = "Test Court";
        Map<String, Object> data = new HashMap<>();
        data.put("courtName", courtName);
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getId()).thenReturn(Long.valueOf(caseReference));
        when(caseDetails.getData()).thenReturn(data);
        when(systemUserService.getSysUserToken()).thenReturn("token");
        when(caseDataService.findCaseById("token", caseReference)).thenReturn(caseDetails);

        SendGridLogMessage logMessage = buildLogMessage(caseReference);
        List<MessageFailureView> result = mapper.convertToEmailFailureView(List.of(logMessage));

        assertEquals(1, result.size());
        MessageFailureView view = result.getFirst();
        assertEquals(caseReference, view.getCaseReference());
        assertEquals(courtName, view.getCourtName());
        assertEquals("Test Subject", view.getSubject());
        assertEquals("FAILED", view.getStatus());
        assertEquals("Bounce", view.getReason());
        assertEquals("to@test.com", view.getToEmailAddress());
        assertEquals("sg-id-123", view.getMessageId());
        assertEquals("tmpl-1", view.getTemplateId());
        assertEquals("TemplateName", view.getTemplateName());
        assertEquals(OffsetDateTime.parse("2024-04-30T10:15:30+01:00"), view.getSentDate());
    }

    @Test
    void testConvertToEmailFailureView_EmptyList() {
        List<MessageFailureView> result = mapper.convertToEmailFailureView(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertToEmailFailureView_NullCaseReference() {
        SendGridLogMessage logMessage = buildLogMessage(null);
        List<MessageFailureView> result = mapper.convertToEmailFailureView(List.of(logMessage));

        assertThat(result).singleElement()
            .extracting(MessageFailureView::getCaseReference, MessageFailureView::getCourtName)
            .containsOnlyNulls();
    }

    @Test
    void testConvertToEmailFailureView_ExceptionInCaseDataService() {
        String caseReference = "1234567890123456";
        SendGridLogMessage logMessage = buildLogMessage(caseReference);
        when(systemUserService.getSysUserToken()).thenReturn("token");
        when(caseDataService.findCaseById(any(), any())).thenThrow(new RuntimeException("fail"));
        List<MessageFailureView> result = mapper.convertToEmailFailureView(List.of(logMessage));

        assertThat(result).singleElement()
            .extracting(MessageFailureView::getCaseReference, MessageFailureView::getCourtName)
            .containsOnlyNulls();
    }

    @Test
    void testConvertToEmailFailureView_NullCourtName() {
        String caseReference = "1234567890123456";
        Map<String, Object> data = new HashMap<>();
        // No courtName in data
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getId()).thenReturn(Long.valueOf(caseReference));
        when(caseDetails.getData()).thenReturn(data);
        when(systemUserService.getSysUserToken()).thenReturn("token");
        when(caseDataService.findCaseById("token", caseReference)).thenReturn(caseDetails);

        SendGridLogMessage logMessage = buildLogMessage(caseReference);
        List<MessageFailureView> result = mapper.convertToEmailFailureView(List.of(logMessage));

        assertThat(result).singleElement()
            .extracting(MessageFailureView::getCaseReference, MessageFailureView::getCourtName)
            .containsExactly(caseReference, null);
    }
}
