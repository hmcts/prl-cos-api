package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FL401ListOnNoticeDirectionsEnum {
    @CCD(label = "Reduced notice period")
    @JsonProperty("reducedNoticedPeriod")
    reducedNoticedPeriod("reducedNoticedPeriod", "Reduced notice period"),
    @CCD(label = "List with Child arrangements case")
    @JsonProperty("listWithChildArrangementCases")
    listWithChildArrangementCases("listWithChildArrangementCases", "List with Child arrangements case"),
    @CCD(label = "Applicant needs to provide further information")
    @JsonProperty("applicantNeedsToProvideInfo")
    applicantNeedsToProvideInfo("applicantNeedsToProvideInfo", "Applicant needs to provide further information"),
    @CCD(label = "The respondent needs to file a statement")
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
