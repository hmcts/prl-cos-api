package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.function.Function;

@Data
@Builder
public class BarristerFilter {
    private String userOrgIdentifier;
    private boolean caseworkerOrSolicitor;
    private boolean isBarrister;
    private boolean caseTypeC100OrFL401;
    private Function<PartyDetails, String> legalRepOrganisation;
}
