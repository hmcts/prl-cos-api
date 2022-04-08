package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class Fl401OtherProceedingsDetails {
    private final String nameOfCourt;
    private final String caseNumber;
    private final String typeOfCase;
    private final String anyOtherDetails;
}
