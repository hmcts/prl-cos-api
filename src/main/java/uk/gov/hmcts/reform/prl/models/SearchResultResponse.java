package uk.gov.hmcts.reform.prl.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@ToString
public class SearchResultResponse {

    int total;
    List<CaseDetails> cases;

}
