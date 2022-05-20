package uk.gov.hmcts.reform.prl.enums.ServiceOfApplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum NoticeOfProceedingsPartiesEnum {

    noticeOfProceedingsParties("Notice of proceedings (C6) (Notice to parties)");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static NoticeOfProceedingsPartiesEnum getValue(String key) {
        return NoticeOfProceedingsPartiesEnum.valueOf(key);
    }

}
