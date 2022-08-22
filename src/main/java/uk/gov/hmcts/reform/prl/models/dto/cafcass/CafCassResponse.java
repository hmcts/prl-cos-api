package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class CafCassResponse {
    public int total;
    public List<CafCassCaseDetail> cases;
}
