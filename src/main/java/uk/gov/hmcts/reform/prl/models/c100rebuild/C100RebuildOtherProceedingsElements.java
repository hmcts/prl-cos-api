package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildOtherProceedingsElements {

    @JsonProperty("op_childrenInvolvedCourtCase")
    private YesOrNo childrenInvolvedCourtCase;
    @JsonProperty("op_courtOrderProtection")
    private YesOrNo courtOrderProtection;
    @JsonProperty("op_courtProceedingsOrders")
    private String[] courtProceedingsOrders;
    @JsonProperty("op_otherProceedings")
    private OtherProceedings otherProceedings;
}