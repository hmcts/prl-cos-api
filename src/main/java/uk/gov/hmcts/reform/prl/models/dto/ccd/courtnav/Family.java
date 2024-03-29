package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class Family {

    private final ApplicationCoverEnum whoApplicationIsFor;
    private final List<ProtectedChild> protectedChildren;

    /**
     * Ongoing proceedings.
     */
    private final boolean anyOngoingCourtProceedings;
    private final List<CourtProceedings> ongoingCourtProceedings;
}
