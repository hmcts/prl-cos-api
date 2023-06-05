package uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public class EmailNotificationDetails {
    @JsonProperty("printedDocs")
    private String printedDocs;
    @JsonProperty("emailAddress")
    private String emailAddress;
    @JsonProperty("timeStamp")
    private String timeStamp;
}
