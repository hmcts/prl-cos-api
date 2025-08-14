package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.AbuseTypes;

@Builder
@Data
public class Behaviours {

    private String abuseNatureDescription;
    private String behavioursStartDateAndLength;
    private String behavioursNature;
    private YesOrNo behavioursApplicantSoughtHelp;
    private String behavioursApplicantHelpSoughtWho;
    private String behavioursApplicantHelpAction;

    private AbuseTypes typesOfAbuse;
    private String natureOfBehaviour;
    private String abuseStartDateAndLength;
    private YesOrNo respondentSoughtHelp;
    private String respondentTypeOfHelp;

}
