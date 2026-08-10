package uk.gov.hmcts.reform.prl.models.dto.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum NotificationType {
    @CCD(label = "Gov notify email")
    @JsonProperty("GOV_NOTIFY_EMAIL")
    GOV_NOTIFY_EMAIL("GOV_NOTIFY_EMAIL", "Gov notify email"),
    @CCD(label = "Sendgrid email")
    @JsonProperty("SENDGRID_EMAIL")
    SENDGRID_EMAIL("SENDGRID_EMAIL", "Sendgrid email"),
    @CCD(label = "Bulk print")
    @JsonProperty("BULK_PRINT")
    BULK_PRINT("BULK_PRINT", "Bulk print");

    private final String code;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static NotificationType getValue(String key) {
        return NotificationType.valueOf(key);
    }
}
