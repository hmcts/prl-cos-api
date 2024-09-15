package uk.gov.hmcts.reform.prl.models.serviceofdocuments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOfDocuments {

    @JsonProperty("sodDocumentsList")
    private List<Element<DocumentsDynamicList>> sodDocumentsList;

    @JsonProperty("sodAdditionalDocumentsList")
    private List<Element<Document>> sodAdditionalDocumentsList;
}
