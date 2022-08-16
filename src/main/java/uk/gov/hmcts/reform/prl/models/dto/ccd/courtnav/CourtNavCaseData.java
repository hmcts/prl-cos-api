package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;


@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CourtNavCaseData implements MappableObject {

    private final BeforeStart beforeStart;


    private final Situation situation;


    /**
     * Applicant details.
     */
    private final ApplicantsDetails applicantDetails;

    /**
     * Respondent Details.
     */
    private final RespondentDetails respondentDetails;

    /**
     * Applicant's Family.
     */
    private final Family family;

    /**
     * Relationship to Respondent.
     */
    private final CourtNavRelationShipToRespondent relationshipWithRespondent;


    /**
     * Respondent's Behaviour.
     */
    private final CourtNavRespondentBehaviour respondentBehaviour;

    /**
     * Home.
     */
    private final TheHome theHome;


    /**
     * Statement of truth.
     */
    private final CourtNavStmtOfTruth statementOfTruth;


    /**
     * Going to court.
     */
    private final GoingToCourt goingToCourt;


}
