package uk.gov.hmcts.reform.prl.models.dto.bulkprint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkPrintDetails {
    @JsonProperty("printedDocs")
    private String printedDocs;
    @JsonProperty("bulkPrintId")
    private String bulkPrintId;
    @JsonProperty("recipientsName")
    private String recipientsName;
    @JsonProperty("postalAddress")
    private Address postalAddress;
    @JsonProperty("timeStamp")
    private String timeStamp;
    @JsonProperty("servedParty")
    private String servedParty;
    @JsonProperty("printDocs")
    private List<Element<Document>> printDocs;
}
