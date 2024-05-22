package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FL401ListOnNoticeDirectionsEnum {
    @JsonProperty("reducedNoticedPeriod")
    reducedNoticedPeriod("reducedNoticedPeriod", "Reduced notice period"),
    @JsonProperty("listWithChildArrangementCases")
    listWithChildArrangementCases("listWithChildArrangementCases", "List with Child arrangements case"),
    @JsonProperty("applicantNeedsToProvideInfo")
    applicantNeedsToProvideInfo("applicantNeedsToProvideInfo", "Applicant needs to provide further information"),
    @JsonProperty("respondentNeedsToFileStatement")
    respondentNeedsToFileStatement("respondentNeedsToFileStatement", "The respondent needs to file a statement");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FL401ListOnNoticeDirectionsEnum getValue(String key) {
        return FL401ListOnNoticeDirectionsEnum.valueOf(key);
    }
}
