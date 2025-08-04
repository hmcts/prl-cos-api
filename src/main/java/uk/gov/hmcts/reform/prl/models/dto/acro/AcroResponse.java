package uk.gov.hmcts.reform.prl.models.dto.acro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@ToString
public class AcroResponse {
    public int total;
    public List<AcroCaseDetail> cases;
}
