package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SdoLanguageDialect {
    @JsonProperty("interpreterNeedFor")
    private final String interpreterNeedFor;
    @JsonProperty("languageOrDialect")
    private final String languageOrDialect;

    @JsonCreator
    public SdoLanguageDialect(String interpreterNeedFor, String languageOrDialect) {
        this.interpreterNeedFor  = interpreterNeedFor;
        this.languageOrDialect  = languageOrDialect;
    }
}
