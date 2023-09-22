package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum C2AdditionalOrdersRequestedDa {

    @JsonProperty("REQUESTING_ADJOURNMENT")
    REQUESTING_ADJOURNMENT(
        "REQUESTING_ADJOURNMENT",
            "Requesting an adjournment for a scheduled hearing"
    ),
    @JsonProperty("OTHER")
    OTHER(
        "OTHER",
        "Other"
    );


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static C2AdditionalOrdersRequestedDa getValue(String key) {
        return C2AdditionalOrdersRequestedDa.valueOf(key);
    }
}
