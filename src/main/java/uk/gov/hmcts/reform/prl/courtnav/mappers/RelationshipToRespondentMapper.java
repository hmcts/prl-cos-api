package uk.gov.hmcts.reform.prl.courtnav.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
public class RelationshipToRespondentMapper {

    public JsonObject map(CourtNavCaseData courtNavCaseData) {

        return new NullAwareJsonObjectBuilder()
            .add("applicantRelationship", courtNavCaseData.getRelationshipDescription().getDisplayedValue())
            .add("relationshipDateComplexStartDate", !(courtNavCaseData.getRelationshipDescription().getDisplayedValue().equals("noneOfAbove"))
                ? String.valueOf(courtNavCaseData.getRelationshipStartDate()) : null)
            .add("relationshipDateComplexEndDate", !(courtNavCaseData.getRelationshipDescription().getDisplayedValue().equals("noneOfAbove"))
                ? String.valueOf(courtNavCaseData.getRelationshipEndDate()) : null)
            .add("applicantRelationshipDate", !(courtNavCaseData.getRelationshipDescription().getDisplayedValue().equals("noneOfAbove"))
                ? String.valueOf(courtNavCaseData.getCeremonyDate()) : null)
            .add("applicantRelationshipOptions", (courtNavCaseData.getRelationshipDescription().getDisplayedValue().equals("noneOfAbove"))
                ? courtNavCaseData.getRespondentsRelationshipToApplicant().getDisplayedValue() : null)
            .add("relationOptionsOther", (courtNavCaseData.getRespondentsRelationshipToApplicant().getDisplayedValue().equals("other"))
                ? courtNavCaseData.getRelationshipToApplicantOther() : null)
            .build();
    }
}
