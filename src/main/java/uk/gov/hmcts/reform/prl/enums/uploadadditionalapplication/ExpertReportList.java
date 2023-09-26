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
public enum ExpertReportList {
    @JsonProperty("pediatric")
    pediatric("pediatric", "Pediatric"),
    @JsonProperty("pediatricRadiologist")
    pediatricRadiologist("pediatricRadiologist", "Pediatric Radiologist");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static ExpertReportList getValue(String key) {
        return ExpertReportList.valueOf(key);
    }
}
