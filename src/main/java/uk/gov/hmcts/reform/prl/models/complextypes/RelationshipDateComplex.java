package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Data
@Builder
@Jacksonized
public class RelationshipDateComplex {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipDateComplexStartDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipDateComplexEndDate;
}
