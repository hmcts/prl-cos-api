package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CourtNavCaseData implements MappableObject {

    private BeforeStart beforeStart;
    private Situation situation;
    private ApplicantsDetails applicantDetails;
    private CourtNavRespondent courtNavRespondent;
    private Family family;
    private CourtNavRelationShipToRespondent relationshipWithRespondent;
    private CourtNavRespondentBehaviour respondentBehaviour;
    private CourtNavHome courtNavHome;
    private CourtNavStatementOfTruth statementOfTruth;
    private GoingToCourt goingToCourt;
}
