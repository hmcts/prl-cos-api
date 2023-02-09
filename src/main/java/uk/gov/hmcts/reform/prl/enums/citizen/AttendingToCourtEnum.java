package uk.gov.hmcts.reform.prl.enums.citizen;

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
public enum AttendingToCourtEnum {

    @JsonProperty("videohearings")
    videohearings("videohearings","Yes, I can take part in video hearings"),
    @JsonProperty("readandwritewelsh")
    phonehearings("readandwritewelsh","Yes, I can take part in phone hearings"),
    @JsonProperty("languageinterpreter")
    nohearings("languageinterpreter","No, I cannot take part in either video or phone hearings");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static AttendingToCourtEnum getValue(String key) {
        return AttendingToCourtEnum.valueOf(key);
    }
}
