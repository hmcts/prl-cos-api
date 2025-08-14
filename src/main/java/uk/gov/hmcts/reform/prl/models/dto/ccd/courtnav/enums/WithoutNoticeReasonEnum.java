package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum WithoutNoticeReasonEnum {

    riskOfSignificantHarm("There is risk of significant harm to the applicant or a relevant child, "
                               + "attributable to conduct of the respondent, if the order is not made immediately"),
    deterredFromPursuingApplication("It is likely that the applicant will be deterred or prevented "
                                           + "from pursuing the application if order is not made immediately"),
    respondentDeliberatelyEvadingService("The applicant believes that the respondent is aware of the proceedings but "
                   + "is deliberately evading service and that the applicant or a relevant child "
                   + "will be seriously prejudiced by the delay in effecting substituted service");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static WithoutNoticeReasonEnum getValue(String key) {
        return WithoutNoticeReasonEnum.valueOf(key);
    }

    public static String mapOptionFromDisplayedValue(String enteredValue) {
        return Arrays.stream(WithoutNoticeReasonEnum.values())
            .filter(i -> i.getDisplayedValue().equals(enteredValue))
            .map(i -> "option" + i.displayedValue)
            .findFirst().orElse("");
    }

    public static String getDisplayedValueFromEnumString(String enteredValue) {
        return Arrays.stream(WithoutNoticeReasonEnum.values())
            .map(i -> WithoutNoticeReasonEnum.valueOf(enteredValue))
            .map(i -> i.displayedValue)
            .findFirst().orElse("");
    }
}
