package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OrdersServingRespondentsEnum {
    @JsonProperty("applicantLegalRepresentative")
    applicantLegalRepresentative("applicantLegalRepresentative", "Applicant's legal representative"),
    @JsonProperty("courtBailiff")
    courtBailiff("courtBailiff", "Court bailiff (you must arrange for them to serve the order)"),
    @JsonProperty("courtAdmin")
    courtAdmin("courtAdmin", "Court admin (you must serve the order)");

    private final String id;
    private final String displayedValue;

    public String getId() {
        return id;
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrdersServingRespondentsEnum getValue(String key) {
        return OrdersServingRespondentsEnum.valueOf(key);
    }
}

