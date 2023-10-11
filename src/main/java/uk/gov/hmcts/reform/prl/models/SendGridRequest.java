package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "sendGridRequestWith")
@Schema(description = "The response object to hearing management")
public class SendGridRequest {

    @JsonProperty("emailProps")
    private Map<String,String> emailProps;

    @JsonProperty("toEmailAddress")
    private String toEmailAddress;

    @JsonProperty("listOfAttachments")
    private List<Document> listOfAttachments;

    @JsonProperty("servedParty")
    private String servedParty;

}
