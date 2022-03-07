package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocTypeOtherDocumentsEnum {

    @JsonProperty("applicantStatement")
    APPLICANTSTATEMENT("applicantStatement", "Applicant statement - for example photographic evidence, witness statement, mobile phone screenshot"),
    @JsonProperty("cafcassReports")
    CAFCASS("cafcassReports", "Cafcass reports"),
    @JsonProperty("expertReports")
    EXPERT("expertReports", "Expert reports"),
    @JsonProperty("respondentReports")
    RESPONDENT("respondentReports", "Respondent reports"),
    @JsonProperty("otherReports")
    OTHER("otherReports", "Other reports");

    private final String id;
    private final String displayedValue;

}
