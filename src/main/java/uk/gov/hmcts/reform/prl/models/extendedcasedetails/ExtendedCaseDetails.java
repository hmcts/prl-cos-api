package uk.gov.hmcts.reform.prl.models.extendedcasedetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;


@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ExtendedCaseDetails {

    private Long id;

    @JsonProperty("case_data")
    private CaseData caseData;

    @JsonProperty("data_classification")
    private Map<String, Object> dataClassification;
}
