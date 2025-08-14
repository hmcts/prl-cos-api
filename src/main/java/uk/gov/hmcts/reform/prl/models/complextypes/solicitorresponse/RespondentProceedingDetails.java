package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class RespondentProceedingDetails {

    private final RespondentProceedingsEnum previousOrOngoingProceedings;
    private final String caseNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateStarted;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateEnded;
    private final List<RespondentTypeOfOrderEnum> typeOfOrder;
    private final String otherTypeOfOrder;
    private final String nameOfJudge;
    private final String nameOfCourt;
    private final String nameOfChildrenInvolved;
    private final String nameOfGuardian;
    private final String nameAndOffice;
    private final Document uploadRelevantOrder;
}
