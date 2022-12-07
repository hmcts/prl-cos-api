package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantReasonableAdjustmentsEnum {

    @JsonProperty("documentFormat")
    DOCUMENTS_SUPPORT("documentFormat", "document format"),

    @JsonProperty("communicationHelp")
    COMMUNICATION_HELP("communicationHelp", "communication help"),

    @JsonProperty("hearingSupport")
    COURT_HEARING_SUPPORT("hearingSupport", "hearing support"),

    @JsonProperty("hearingComfort")
    COURT_HEARING_COMFORT("hearingComfort", "hearing comfort"),

    @JsonProperty("travelHelp")
    TRAVELLING_TO_COURT("travelHelp", "travel help"),

    @JsonProperty("unableToTakeCourtProceedings")
    UNABLE_TO_TAKE_COURT_PROCEEDINGS("unableToTakeCourtProceedings", "unable to take court proceedings"),

    NO_NEED_OF_SUPPORT("noNeedOfSupport", "no need of support");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicantReasonableAdjustmentsEnum getValue(String key) {
        return ApplicantReasonableAdjustmentsEnum.valueOf(key);
    }
}
