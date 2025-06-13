package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtNavRelationShipToRespondent {

    private ApplicantRelationshipDescriptionEnum relationshipDescription;
    private CourtNavDate relationshipStartDate;
    private CourtNavDate relationshipEndDate;
    private CourtNavDate ceremonyDate;
    private ApplicantRelationshipOptionsEnum respondentsRelationshipToApplicant;
    private String respondentsRelationshipToApplicantOther;
    private boolean anyChildren;

}
