package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
@Jacksonized
public class RespondentRelationDateInfo {
    @CCD(
            label = "When did their relationship start and when did it end? If unknown, please give an approximate date",
            searchable = false
    )
    private  final  RelationshipDateComplex relationStartAndEndComplexType;
    @CCD(
            label = "If the applicant is or was married or in a civil partnership with the respondent, what date was the wedding or civil ceremony?",
            searchable = false
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate applicantRelationshipDate;
}
