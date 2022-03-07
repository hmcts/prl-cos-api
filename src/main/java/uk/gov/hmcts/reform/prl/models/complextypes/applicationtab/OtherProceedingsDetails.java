package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class OtherProceedingsDetails {

    private final String previousOrOngoingProceedings;
    private final String caseNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateStarted;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateEnded;
    private final String typeOfOrder;
    private final String otherTypeOfOrder;
    private final String nameOfJudge;
    private final String nameOfCourt;
    private final String nameOfChildrenInvolved;
    private final String nameOfGuardian;
    private final String nameAndOffice;


}
