package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OtherProceedings {

    @CCD(ignore = true)
    @JsonProperty("order")
    private OrderDetails order;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Are there previous or ongoing proceedings for the child(ren)?", searchable = false)
  private String previousOrOngoingProceedings;
  // ==== end synthesised definition-only fields ====
}