package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("beforeStart")
    private BeforeStart beforeStart;

    @JsonProperty("situation")
    private Situation situation;

    @JsonProperty("applicantDetails")
    private CourtNavApplicant courtNavApplicant;

    @JsonProperty("respondentDetails")
    private CourtNavRespondent courtNavRespondent;

    @JsonProperty("family")
    private Family family;

    @JsonProperty("relationshipWithRespondent")
    private CourtNavRelationShipToRespondent relationshipWithRespondent;

    @JsonProperty("respondentBehaviour")
    private CourtNavRespondentBehaviour respondentBehaviour;

    @JsonProperty("theHome")
    private CourtNavHome courtNavHome;

    @JsonProperty("statementOfTruth")
    private CourtNavStatementOfTruth statementOfTruth;

    @JsonProperty("goingToCourt")
    private GoingToCourt goingToCourt;
}
