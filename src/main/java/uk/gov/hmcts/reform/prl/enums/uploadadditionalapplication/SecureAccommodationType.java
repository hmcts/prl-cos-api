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
public enum SecureAccommodationType {

    @JsonProperty("ENGLAND")
    ENGLAND("ENGLAND", "England"),
    @JsonProperty("WALES")
    WALES("WALES", "Wales");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static SecureAccommodationType getValue(String key) {
        return SecureAccommodationType.valueOf(key);
    }

}
