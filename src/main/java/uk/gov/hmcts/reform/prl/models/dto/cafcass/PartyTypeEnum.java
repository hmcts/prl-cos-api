package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyTypeEnum {

    @JsonProperty("applicant")
    APPLICANT("APPLICANT", "Applicant"),
    @JsonProperty("RESPONDENT")
    RESPONDENT("RESPONDENT", "Respondent"),
    @JsonProperty("OtherPeople")
    OTHERPEOPLE("OTHERPEOPLE", "Other People");

    private final String id;
    private final String displayedValue;

}
