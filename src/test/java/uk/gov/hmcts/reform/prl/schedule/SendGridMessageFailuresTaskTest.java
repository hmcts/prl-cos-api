package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.MessageFailureOrchestrator;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;

class SendGridMessageFailuresTaskTest {
    private MessageFailureOrchestrator messageFailureOrchestrator;
    private SendGridMessageFailuresTask sendGridMessageFailuresTask;

    @BeforeEach
    void setUp() {
        messageFailureOrchestrator = Mockito.mock(MessageFailureOrchestrator.class);
        Clock fixedClock = Clock.fixed(
            Instant.parse("2026-04-30T02:00:00Z"),
            ZoneId.systemDefault()
        );
        sendGridMessageFailuresTask = new SendGridMessageFailuresTask(messageFailureOrchestrator, fixedClock);
        ReflectionTestUtils.setField(sendGridMessageFailuresTask, "daysToQuery", 1);
    }

    @Test
    void testRun() {
        sendGridMessageFailuresTask.run();

        String expectedQuery = "sg_message_id_created_at >= TIMESTAMP \"2026-04-28T23:00:00Z\" AND "
            + "sg_message_id_created_at < TIMESTAMP \"2026-04-29T23:00:00Z\" AND "
            + "status IN ('dropped', 'blocked', 'bounced')";
        verify(messageFailureOrchestrator).processQuery(expectedQuery);
    }
}
