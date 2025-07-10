package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Family {

    private ApplicationCoverEnum whoApplicationIsFor;
    private List<ProtectedChild> protectedChildren;
    private boolean anyOngoingCourtProceedings;
    private List<CourtProceedings> ongoingCourtProceedings;
}
