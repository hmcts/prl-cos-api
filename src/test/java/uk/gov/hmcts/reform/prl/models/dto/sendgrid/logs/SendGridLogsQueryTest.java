package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendGridLogsQueryTest {

    @Test
    void testAfterCondition() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        String query = SendGridLogsQuery.builder()
            .after(now)
            .build();

        assertThat(query).isEqualTo("sg_message_id_created_at > TIMESTAMP \"2024-01-01T00:00:00Z\"");
    }

    @Test
    void testOnOrAfterCondition() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        String query = SendGridLogsQuery.builder()
            .onOrAfter(now)
            .build();

        assertThat(query).isEqualTo("sg_message_id_created_at >= TIMESTAMP \"2024-01-01T00:00:00Z\"");
    }

    @Test
    void testBeforeCondition() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        String query = SendGridLogsQuery.builder()
            .before(now)
            .build();

        assertThat(query).isEqualTo("sg_message_id_created_at < TIMESTAMP \"2024-01-01T00:00:00Z\"");
    }

    @Test
    void testInCondition() {
        String query = SendGridLogsQuery.builder()
            .in(SendGridLogsQueryField.STATUS, List.of("delivered", "bounced"))
            .build();

        assertThat(query).isEqualTo("status IN ('delivered', 'bounced')");
    }


    @Test
    void testMultipleConditions() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        String query = SendGridLogsQuery.builder()
            .after(now)
            .before(now)
            .in(SendGridLogsQueryField.STATUS, Collections.singletonList("dropped"))
            .build();

        assertThat(query).isEqualTo("sg_message_id_created_at > TIMESTAMP \"2024-01-01T00:00:00Z\" AND "
                                        + "sg_message_id_created_at < TIMESTAMP \"2024-01-01T00:00:00Z\" AND "
                                        + "status IN ('dropped')");
    }

    @Test
    void testInConditionWithEmptyValuesThrows() {
        SendGridLogsQuery sendGridLogsQuery = SendGridLogsQuery.builder();
        List<String> values = Collections.emptyList();
        assertThatThrownBy(() -> sendGridLogsQuery.in(SendGridLogsQueryField.STATUS, values))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IN clause requires at least one value");
    }

    @Test
    void testBuildWithNoConditionsThrows() {
        SendGridLogsQuery sendGridLogsQuery = SendGridLogsQuery.builder();
        assertThatThrownBy(sendGridLogsQuery::build)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No conditions defined for SendGrid query");
    }
}

