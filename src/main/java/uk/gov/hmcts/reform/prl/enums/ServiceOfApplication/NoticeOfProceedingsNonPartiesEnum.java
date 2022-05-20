package uk.gov.hmcts.reform.prl.enums.ServiceOfApplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum NoticeOfProceedingsNonPartiesEnum {

    noticeOfProceedingsNonParties("Notice of proceedings (C6a) (Notice to non-parties)");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static NoticeOfProceedingsNonPartiesEnum getValue(String key) {
        return NoticeOfProceedingsNonPartiesEnum.valueOf(key);
    }

}
