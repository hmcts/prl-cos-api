package uk.gov.hmcts.reform.prl.enums.caseworkeremailnotification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CaseWorkerEmailNotificationEventEnum {

    @JsonProperty("notifyLocalCourt")
    notifyLocalCourt("notifyLocalCourt", "Notify court");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CaseWorkerEmailNotificationEventEnum getValue(String key) {
        return CaseWorkerEmailNotificationEventEnum.valueOf(key);
    }

}
