package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum FurtherEvidenceType {
    @JsonProperty("APPLICANT_STATEMENT")
    APPLICANT_STATEMENT(
        "APPLICANT_STATEMENT",
        "Applicant statement - for example witness, social work, initial or position statements, or police disclosure documents"
    ),
    @JsonProperty("GUARDIAN_REPORTS")
    GUARDIAN_REPORTS("GUARDIAN_REPORTS", "Child's guardian reports"),
    @JsonProperty("EXPERT_REPORTS")
    EXPERT_REPORTS("EXPERT_REPORTS", "Expert reports"),
    @JsonProperty("OTHER_REPORTS")
    OTHER_REPORTS("OTHER_REPORTS", "Other reports"),
    @JsonProperty("NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE")
    NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE("NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE", "Notice of Acting / Notice of Issue");


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static FurtherEvidenceType getValue(String key) {
        return FurtherEvidenceType.valueOf(key);
    }
}
