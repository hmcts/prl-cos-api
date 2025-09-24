package uk.gov.hmcts.reform.prl.models.dto.ccd.request.acro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Term {

    @JsonProperty("data.orderCollection.value.orderTypeId.keyword")
    private String orderTypeId;
}
