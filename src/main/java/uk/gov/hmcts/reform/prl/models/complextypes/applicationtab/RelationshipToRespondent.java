package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Builder
@Data
public class RelationshipToRespondent {
    @CCD(label = "Applicants relationship to respondent:", searchable = false)
    private final String applicantRelationship;
    @CCD(label = "When did the relationship start?", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipDateComplexStartDate;
    @CCD(label = "When did the relationship end ( if applicable? )", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipDateComplexEndDate;
    @CCD(
            label = "If the applicant is, or was, married or ina civil partnership with the respondent, what date was the wedding or civil ceremony? ( optional )",
            searchable = false
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate applicantRelationshipDate;
    @CCD(
            label = "What was the respondent’s relationship with the applicant ( if not already answered in this section? )",
            searchable = false
    )
    private final String applicantRelationshipOptions;
}
