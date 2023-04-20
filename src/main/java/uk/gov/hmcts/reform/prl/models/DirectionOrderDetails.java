package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class DirectionOrderDetails {

    private final String preamblesList;
    private final String hearingsAndNextStepsList;
    private final String cafcassOrCymruList;
    private final String localAuthorityList;
    private final String courtList;
    private final String documentationAndEvidenceList;
    private final String furtherList;
    private final String otherList;

    private final String permissionHearingDirectionsText;
    private final HearingData permissionHearingDetails;

}
