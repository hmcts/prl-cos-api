package uk.gov.hmcts.reform.prl.enums.solicitoremailnotification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SolicitorEmailNotificationEventEnum {

    @JsonProperty("awaitingPayment")
    awaitingPayment("awaitingPayment", "awaiting payment"),
    @JsonProperty("fl401SendEmailNotification")
    fl401SendEmailNotification("fl401SendEmailNotification", "fl401 notification");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SolicitorEmailNotificationEventEnum getValue(String key) {
        return SolicitorEmailNotificationEventEnum.valueOf(key);
    }

}
