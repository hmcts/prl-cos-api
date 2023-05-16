package uk.gov.hmcts.reform.prl.models.dto.hearings;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "caseLinkedDataWith")
@Schema(description = "The response object to hearing management")
public class CaseLinkedData {
    public String caseReference;
    public String caseName;
    public List<String> reasonsForLink;
}

