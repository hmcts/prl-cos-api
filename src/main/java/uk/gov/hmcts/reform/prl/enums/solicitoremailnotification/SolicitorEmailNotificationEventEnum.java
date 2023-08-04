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
    fl401SendEmailNotification("fl401SendEmailNotification", "fl401 notification"),
    @JsonProperty("notifyRpa")
    notifyRpa("notifyRpa", "RPA notification"),
    @JsonProperty("withdrawC100")
    withdrawC100("withdrawC100", "C100 case withdraw"),
    @JsonProperty("withdrawFL401")
    withdrawFL401("withdrawFL401", "Fl401 case withdraw"),
    @JsonProperty("withdrawC100BeforeIssue")
    withdrawC100BeforeIssue("withdrawC100BeforeIssue", "C100 case withdraw before issue"),
    @JsonProperty("withdrawFL401BeforeIssue")
    withdrawFL401BeforeIssue("withdrawFL401BeforeIssue", "Fl401 case withdraw before issue");


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
