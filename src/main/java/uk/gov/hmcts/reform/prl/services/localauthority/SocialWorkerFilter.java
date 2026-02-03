package uk.gov.hmcts.reform.prl.services.localauthority;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.function.Function;

@Data
@Builder
public class SocialWorkerFilter {
    private String userOrgIdentifier;
    private boolean isSocialWorker;
    private boolean caseTypeC100OrFL401;
    private Function<PartyDetails, String> localAuthorityOrganisation;
}
