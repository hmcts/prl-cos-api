package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum C2Consent {

    @CCD(label = "Without consent")
    @JsonProperty("withoutConsent")
    withoutConsent("withoutConsent", "Without consent"),
    @CCD(label = "With consent")
    @JsonProperty("withConsent")
    withConsent("withConsent", "With consent");


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static C2Consent getValue(String key) {
        return C2Consent.valueOf(key);
    }
}
