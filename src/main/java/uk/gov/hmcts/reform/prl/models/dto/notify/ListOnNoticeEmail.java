package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ListOnNoticeEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("fullName")
    private final String fullName;
    @JsonProperty("caseNote")
    private final String caseNote;


    @Builder
    public ListOnNoticeEmail(String caseReference,
                             String caseName,
                             String fullName,
                             String caseNote) {
        super(caseReference);
        this.caseName = caseName;
        this.fullName = fullName;
        this.caseNote = caseNote;
    }
}
