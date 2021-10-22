package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EmailTemplateVars {

    @JsonProperty("caseReference")
    private String caseReference;
}
