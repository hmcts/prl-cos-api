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
public enum C2Consent {

    @JsonProperty("withoutConsent")
    withoutConsent("withoutConsent", "Without consent"),
    @JsonProperty("withConsent")
    withConsent("withConsent", "With consent");


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static C2Consent getValue(String key) {
        return C2Consent.valueOf(key);
    }
}
