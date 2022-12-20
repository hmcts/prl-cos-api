package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServingRespondentsEnum {
    @JsonProperty("applicantLegalRepresentative")
    APPLICANT_LEGAL_REPRESENTATIVE("applicantLegalRepresentative", "Applicant's legal representative"),
    @JsonProperty("courtBailiff")
    COURT_BAILIFF("courtBailiff", "Court bailiff"),
    @JsonProperty("unrepresentedApplicant")
    UNREPRESENTED_APPLICANT("unrepresentedApplicant", "Unrepresented applicant who is arranging service"),
    @JsonProperty("courtAdmin")
    COURT_ADMIN("courtAdmin", "Court admin");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ServingRespondentsEnum getValue(String key) {
        return ServingRespondentsEnum.valueOf(key);
    }
}
