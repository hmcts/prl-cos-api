package uk.gov.hmcts.reform.prl.utils.caseflags;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.PartyFlags;

import java.util.Collections;

@Component
public class PartyLevelCaseFlagsGenerator {
    public PartyFlags generatePartyFlags(String partyName, String caseDataField, String roleOnCase) {
        final Flags partyInternalFlag = Flags
            .builder()
            .partyName(partyName)
            .roleOnCase(roleOnCase)
            .visibility("Internal")
            .groupId(caseDataField)
            .details(Collections.emptyList())
            .build();
        final Flags partyExternalFlag = Flags
            .builder()
            .partyName(partyName)
            .roleOnCase(roleOnCase)
            .visibility("External")
            .groupId(caseDataField)
            .details(Collections.emptyList())
            .build();
        PartyFlags partyFlags = PartyFlags.builder()
            .partyInternalFlags(partyInternalFlag)
            .partyExternalFlags(partyExternalFlag)
            .build();
        return partyFlags;
    }
}
