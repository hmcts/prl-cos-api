package uk.gov.hmcts.reform.prl.models.dto.acro;

import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.time.LocalDateTime;

@Data
public class AcroCsvWriterRequest {

    private final PartyDetails applicantsFL401;
    private final PartyDetails respondentsFL401;
    private final String caseId;
    private final String fileName;
    private final String courtName;
    private final String courtId;
    private final LocalDateTime orderExpiryDate;
    private final LocalDateTime nextHearingDate;
}
