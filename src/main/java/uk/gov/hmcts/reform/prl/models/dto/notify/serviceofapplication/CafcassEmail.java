package uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
public class CafcassEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("caseLink")
    private final String caseLink;

    @Builder
    public CafcassEmail(String caseReference,
                        String caseName,
                        String caseLink) {

        super(caseReference);
        this.caseName = caseName;
        this.caseLink = caseLink;
    }
}
