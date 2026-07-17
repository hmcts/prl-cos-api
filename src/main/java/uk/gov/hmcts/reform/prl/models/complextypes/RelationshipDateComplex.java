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
public class RelationshipDateComplex {
    @CCD(label = "Start", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipDateComplexStartDate;
    @CCD(label = "End", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipDateComplexEndDate;
}
