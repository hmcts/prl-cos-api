package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

public enum SendGridLogsQueryField {
    CREATED_AT("sg_message_id_created_at"),
    STATUS("status");

    private final String value;

    SendGridLogsQueryField(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
