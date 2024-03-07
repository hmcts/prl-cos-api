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
public enum ConfidentialDocument {

    @JsonProperty("CONFIDENTIAL")
    CONFIDENTIAL("CONFIDENTIAL", "Yes - restrict to this group");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static ConfidentialDocument getValue(String key) {
        return ConfidentialDocument.valueOf(key);
    }

}
