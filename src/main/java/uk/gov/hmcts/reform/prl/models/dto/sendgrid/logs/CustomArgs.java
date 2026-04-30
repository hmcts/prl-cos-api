package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomArgs {
    private String caseReference;

    @JsonProperty("sg_template_id")
    private String sgTemplateId;

    @JsonProperty("sg_template_name")
    private String sgTemplateName;
}
