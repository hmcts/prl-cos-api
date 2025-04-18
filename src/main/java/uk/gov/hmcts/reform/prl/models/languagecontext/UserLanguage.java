package uk.gov.hmcts.reform.prl.models.languagecontext;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLanguage {

    @JsonProperty("language")
    private String language;
}
