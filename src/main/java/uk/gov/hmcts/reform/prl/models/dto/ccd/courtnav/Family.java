package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class Family {

    private final YesOrNo whoApplicationIsFor;
    private final List<Element<ProtectedChild>> protectedChildren;

    /**
     * Ongoing proceedings.
     */
    private final boolean anyOngoingCourtProceedings;
    private final List<Element<CourtProceedings>> ongoingCourtProceedings;
}
