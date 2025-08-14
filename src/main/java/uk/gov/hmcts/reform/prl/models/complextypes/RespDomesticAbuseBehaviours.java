package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespDomesticAbuseBehaviours {

    private TypeOfAbuseEnum respTypeOfAbuse;

    private String respAbuseNatureDescription;

    private String respBehavioursStartDateAndLength;

    private YesOrNo respBehavioursApplicantSoughtHelp;

    private String respBehavioursApplicantHelpSoughtWho;


}
