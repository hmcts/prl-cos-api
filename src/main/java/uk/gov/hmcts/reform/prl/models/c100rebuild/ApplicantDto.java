package uk.gov.hmcts.reform.prl.models.c100rebuild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantDto {

    private AbuseDto childrenConcernedAbout;
    private String behaviourDetails;
    private String behaviourStartDate;
    private YesOrNo isOngoingBehaviour;
    private YesOrNo seekHelpFromPersonOrAgency;
    private String seekHelpDetails;
}