package uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public class EmailNotificationDetails {
    @JsonProperty("attachedDocs")
    private String attachedDocs;
    @JsonProperty("emailAddress")
    private String emailAddress;
    @JsonProperty("timeStamp")
    private String timeStamp;
    @JsonProperty("docs")
    private List<Element<Document>> docs;
}
