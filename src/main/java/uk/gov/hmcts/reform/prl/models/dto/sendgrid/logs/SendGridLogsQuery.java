package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SendGridLogsQuery {

    private final List<String> conditions = new ArrayList<>();

    public static SendGridLogsQuery builder() {
        return new SendGridLogsQuery();
    }

    public SendGridLogsQuery after(Instant timestamp) {
        conditions.add(SendGridLogsQueryField.CREATED_AT.value() + " > " + timestamp(timestamp));
        return this;
    }

    public SendGridLogsQuery onOrAfter(Instant timestamp) {
        conditions.add(SendGridLogsQueryField.CREATED_AT.value() + " >= " + timestamp(timestamp));
        return this;
    }

    public SendGridLogsQuery before(Instant timestamp) {
        conditions.add(SendGridLogsQueryField.CREATED_AT.value() + " < " + timestamp(timestamp));
        return this;
    }

    public SendGridLogsQuery in(SendGridLogsQueryField field, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("IN clause requires at least one value");
        }

        String joined = values.stream()
            .map(v -> "'" + escape(v) + "'")
            .collect(Collectors.joining(", "));

        conditions.add(field.value() + " IN (" + joined + ")");
        return this;
    }

    public String build() {
        if (conditions.isEmpty()) {
            throw new IllegalStateException("No conditions defined for SendGrid query");
        }

        return String.join(" AND ", conditions);
    }

    private String timestamp(Instant instant) {
        return "TIMESTAMP \"" + instant.toString() + "\"";
    }

    private String escape(String value) {
        return value.replace("'", "\\'");
    }
}
