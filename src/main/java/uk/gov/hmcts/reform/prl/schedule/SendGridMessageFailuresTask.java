package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogsQuery;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogsQueryField;
import uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.MessageFailureOrchestrator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendGridMessageFailuresTask implements Runnable {

    @Value("${send-grid.email-logs-api.days-to-query:1}")
    private int daysToQuery;

    private final MessageFailureOrchestrator messageFailureOrchestrator;
    private final Clock clock;

    @Override
    public void run() {
        log.info("SendGrid message failures task started");

        String query = createQuery();
        messageFailureOrchestrator.processQuery(query);

        log.info("SendGrid message failures task completed");
    }

    private String createQuery() {
        Instant from = getNow()
            .minusDays(daysToQuery)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant();

        Instant to = getNow()
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant();

        return SendGridLogsQuery.builder()
            .onOrAfter(from)
            .before(to)
            .in(SendGridLogsQueryField.STATUS, List.of("dropped", "blocked", "bounced"))
            .build();
    }

    private LocalDate getNow() {
        return LocalDate.now(clock);
    }
}
