package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.Classification;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class CafCassCaseDetail {
    private Long id;

    private String jurisdiction;

    @JsonProperty("case_type_id")
    @JsonAlias("case_type")
    private String caseTypeId;

    @JsonProperty("created_date")
    @JsonAlias("created_on")
    private LocalDateTime createdDate;

    @JsonProperty("last_modified")
    @JsonAlias("last_modified_on")
    private LocalDateTime lastModified;

    @JsonProperty("last_state_modified_date")
    private LocalDateTime lastStateModifiedDate;

    @JsonProperty("case_data")
    private CaseData cases;

    private String state;

    public String caseTypeOfApplication;

}
