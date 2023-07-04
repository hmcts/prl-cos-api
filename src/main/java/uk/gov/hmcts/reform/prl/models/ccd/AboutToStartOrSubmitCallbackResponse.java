package uk.gov.hmcts.reform.prl.models.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.List;
import java.util.Map;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class AboutToStartOrSubmitCallbackResponse implements CallbackResponse {
    private Map<String, Object> data;

    @JsonProperty("data_classification")
    private Map<String, Object> dataClassification;

    @JsonProperty("security_classification")
    private String securityClassification;

    private List<String> errors;

    private List<String> warnings;

    private String state;

    public AboutToStartOrSubmitCallbackResponse(
        Map<String, Object> data,
        Map<String, Object> dataClassification,
        String securityClassification,
        List<String> errors,
        List<String> warnings,
        String state
    ) {
        this.data = data;
        this.dataClassification = dataClassification;
        this.securityClassification = securityClassification;
        this.errors = errors;
        this.warnings = warnings;
        this.state = state;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getDataClassification() {
        return dataClassification;
    }

    @JsonProperty("data_classification")
    public void setDataClassification(Map<String, Object> dataClassification) {
        this.dataClassification = dataClassification;
    }

    public String getSecurityClassification() {
        return securityClassification;
    }

    @JsonProperty("security_classification")
    public void setSecurityClassification(String securityClassification) {
        this.securityClassification = securityClassification;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
