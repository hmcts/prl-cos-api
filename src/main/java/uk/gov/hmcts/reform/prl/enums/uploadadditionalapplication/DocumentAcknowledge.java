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
public enum DocumentAcknowledge {
    @JsonProperty("ACK_RELATED_TO_CASE")
    ACK_RELATED_TO_CASE("ACK_RELATED_TO_CASE", "Yes");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static DocumentAcknowledge getValue(String key) {
        return DocumentAcknowledge.valueOf(key);
    }
}
