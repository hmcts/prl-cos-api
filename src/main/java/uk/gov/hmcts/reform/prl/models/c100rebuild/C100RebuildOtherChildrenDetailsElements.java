package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class C100RebuildOtherChildrenDetailsElements {

    @JsonProperty("ocd_otherChildren")
    private List<ChildDetail> otherChildrenDetails;
    @JsonProperty("ocd_hasOtherChildren")
    private String hasOtherChildren;

}
