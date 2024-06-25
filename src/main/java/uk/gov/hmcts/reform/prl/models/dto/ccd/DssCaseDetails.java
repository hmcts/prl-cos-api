package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DssCaseDetails {
    private final List<Element<Document>> dssUploadedDocuments;
    private final List<Element<Document>> dssUploadedAdditionalDocuments;
    @JsonProperty("edgeCaseTypeOfApplication")
    private final String edgeCaseTypeOfApplication;
    @JsonProperty("dssCaseIsFree")
    private final String dssCaseIsFree;
    @JsonProperty("selectedCourt")
    private final String selectedCourt;
}
