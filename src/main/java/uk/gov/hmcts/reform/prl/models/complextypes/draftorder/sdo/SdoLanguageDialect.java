package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class SdoLanguageDialect {
    @CCD(label = "Name of the person who requires an interpreter", searchable = false)
    @JsonProperty("interpreterNeedFor")
    private final String interpreterNeedFor;
    @CCD(label = "Language or dialect required", searchable = false)
    @JsonProperty("languageOrDialect")
    private final String languageOrDialect;

    @JsonCreator
    public SdoLanguageDialect(String interpreterNeedFor, String languageOrDialect) {
        this.interpreterNeedFor  = interpreterNeedFor;
        this.languageOrDialect  = languageOrDialect;
    }
}
