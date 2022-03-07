package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ReasonForOrderWithoutGivingNoticeEnum {

    harmToApplicantOrChild("There is risk of significant harm to the applicant or a relevant child, "
                               + "attributable to conduct of the respondent, if the order is not made immediately"),
    deferringApplicationIfNotImmediate("It is likely that the applicant will be deterred or prevented "
                                           + "from pursuing the application if order is not made immediately"),
    prejudiced("The applicant believes that the respondent is aware of the proceedings but "
                   + "is deliberately evading service and that the applicant or a relevant child "
                   + "will be seriously prejudiced by the delay in effecting substituted service");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ReasonForOrderWithoutGivingNoticeEnum getValue(String key) {
        return ReasonForOrderWithoutGivingNoticeEnum.valueOf(key);
    }

}
