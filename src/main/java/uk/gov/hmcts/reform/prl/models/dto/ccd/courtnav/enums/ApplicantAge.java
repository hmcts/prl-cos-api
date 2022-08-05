package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantAge {

    @JsonProperty("eighteenOrOlder")
    eighteenOrOlder("eighteenOrOlder", "18 years old or older. You can continue to apply using this form"),

    @JsonProperty("sixteenToEighteen")
    sixteenToEighteen("sixteenToEighteen",
                          "16 to 18 years old. Someone over 18 must help you apply, such as a parent. "
                              + "They will also need to complete form 'FP9' to include with your application."),

    @JsonProperty("underSixteen")
    underSixteen("underSixteen", "Under 16 years old. You will need permission from the court "
                    + "to apply. With the help of someone over 18, you must also "
                    + "complete form 'FP2' and they will need to complete form 'FP9' "
                    + "and include these with your application. Visit GOV.UK and "
                    + "search form ‘FP2’ and form 'FP9'");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicantAge getValue(String key) {
        return ApplicantAge.valueOf(key);
    }
}
