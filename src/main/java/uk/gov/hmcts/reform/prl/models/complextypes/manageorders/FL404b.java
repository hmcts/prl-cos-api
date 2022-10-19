package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Data
public class FL404b {

    private final String fl404bCourtName;
    private final Address fl404bCourtAddress;
    private final String fl404bCaseNumber;
    private final String fl404bApplicantName;
    private final String fl404bApplicantReference;
    private final String fl404bRespondentName;
    private final String fl404bRespondentReference;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate fl404bRespondentDob;
    private final Address fl404bRespondentAddress;
    private final String fl404bHearingOutcome;


}
