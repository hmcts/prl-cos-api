package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@Jacksonized
public class CourtNavRelationShipToRespondent {

    private final ApplicantRelationshipDescriptionEnum relationshipDescription;
    private final CourtNavDate relationshipStartDate;
    private final CourtNavDate relationshipEndDate;
    private final CourtNavDate ceremonyDate;
    private final ApplicantRelationshipOptionsEnum respondentsRelationshipToApplicant;
    private final String relationshipToApplicantOther;
    private final boolean anyChildren;

}
