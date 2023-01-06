package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SdoLanguageDialect {
    private final String interpreterNeedFor;
    private final String languageOrDialect;
}
