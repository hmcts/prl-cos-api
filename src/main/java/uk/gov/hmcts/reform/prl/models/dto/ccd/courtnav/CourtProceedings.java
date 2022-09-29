package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CourtProceedings {

    private final String nameOfCourt;
    private final String caseNumber;
    private final String caseType;
    private final String caseDetails;
}
