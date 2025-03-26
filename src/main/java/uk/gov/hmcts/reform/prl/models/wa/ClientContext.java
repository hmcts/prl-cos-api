package uk.gov.hmcts.reform.prl.models.wa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.languagecontext.UserLanguage;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientContext {
    @JsonProperty("user_task")
    private UserTask userTask;
    @JsonProperty("user_language")
    private UserLanguage userLanguage;
}
