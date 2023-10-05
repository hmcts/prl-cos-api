package uk.gov.hmcts.reform.prl.utils.caseflags;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.PartyFlags;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class PartyLevelCaseFlagsGenerator {
    public Map<String, Object> generateFlags(String partyName, String caseDataField, String roleOnCase) {
        Map<String, Object> data = new HashMap<>();
        final Flags partyInternalFlag = Flags
                .builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .details(Collections.emptyList())
                .build();
        final Flags partyExternalFlag = Flags
                .builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .details(Collections.emptyList())
                .build();
        PartyFlags partyFlags = PartyFlags.builder()
                .partyInternalFlags(partyInternalFlag)
                .partyExternalFlags(partyExternalFlag)
                .build();

        data.put(caseDataField, partyFlags);

        return data;
    }
}
