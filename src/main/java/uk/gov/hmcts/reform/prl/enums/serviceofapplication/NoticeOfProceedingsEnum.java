package uk.gov.hmcts.reform.prl.enums.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum NoticeOfProceedingsEnum {

    noticeOfProceedings("Notice of proceedings (FL402)");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static NoticeOfProceedingsEnum getValue(String key) {
        return NoticeOfProceedingsEnum.valueOf(key);
    }

}
