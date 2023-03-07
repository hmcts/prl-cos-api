package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;


@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildAbuseBehaviour {


    private ChildAbuseEnum typeOfAbuse;

    private String abuseNatureDescription;


    private String behavioursStartDateAndLength;


    private YesOrNo behavioursApplicantSoughtHelp;


    private String behavioursApplicantHelpSoughtWho;


    private YesOrNo allChildrenAreRisk;

    private String whichChildrenAreRisk;
}
