package uk.gov.hmcts.reform.prl.utils.caseflags;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.PartyFlags;

import java.util.Collections;

@Slf4j
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
        log.info("Party flag is now generated for ::" + partyName);
        return partyFlags;
    }
}
