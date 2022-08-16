package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicationCoverEnum {

    @JsonProperty("applicantOnly")
    applicantOnly("applicantOnly", "Applicant Only"),

    @JsonProperty("applicantAndChildren")
    applicantAndChildren("applicantAndChildren", "Applicant and children ");

    private final String id;
    private final String displayedValue;

    public String getId() {
        return id;
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicationCoverEnum getValue(String key) {
        return ApplicationCoverEnum.valueOf(key);
    }
}
