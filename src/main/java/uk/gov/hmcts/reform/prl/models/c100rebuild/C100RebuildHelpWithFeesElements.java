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
public class C100RebuildHelpWithFeesElements {

    @JsonProperty("hwf_needHelpWithFees")
    private YesOrNo needHelpWithFees;
    @JsonProperty("hwf_feesAppliedDetails")
    private YesOrNo feesAppliedDetails;

    private String helpWithFeesReferenceNumber;

}
