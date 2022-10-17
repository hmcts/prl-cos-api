package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespondentSolicitorMiam {
    @JsonProperty("whatIsMiamPlaceHolder")
    private final String whatIsMiamPlaceHolder;
    public RespondentSolicitorMiam(String whatIsMiamPlaceHolder){
        this.whatIsMiamPlaceHolder = whatIsMiamPlaceHolder;
    }

}

