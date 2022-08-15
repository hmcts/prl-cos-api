package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum;

import java.time.LocalDate;

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
    private final ApplicantRelationshipDescriptionEnum relationshipDescription;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipStartDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipEndDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ceremonyDate;
    private final ApplicantRelationshipOptionsEnum respondentsRelationshipToApplicant;
    private final String relationshipToApplicantOther;
    private final boolean anyChildren;

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
