package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProceedingDetails {

    private final ProceedingsEnum previousOrOngoingProceedings;
    private final String caseNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateStarted;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateEnded;
    private final List<TypeOfOrderEnum> typeOfOrder;
    private final String otherTypeOfOrder;
    private final String nameOfJudge;
    private final String nameOfCourt;
    private final String nameOfChildrenInvolved;
    private final String nameOfGuardian;
    private final String nameAndOffice;
    private Document uploadRelevantOrder;
    private int index;

}
