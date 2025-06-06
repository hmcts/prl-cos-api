package uk.gov.hmcts.reform.prl.models.wa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaMapper {

    @JsonProperty("client_context")
    private ClientContext clientContext;

}
