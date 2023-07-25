package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@AllArgsConstructor
@Getter
@Data
@Builder
public class DocumentRequest {

    @JsonProperty("typeOfUpload")
    private final TypeOfDocumentUpload typeOfUpload;
    @JsonProperty("caseId")
    private final String caseId;
    @JsonProperty("categoryId")
    private final String categoryId;
    @JsonProperty("partyId")
    private final String partyId;
    @JsonProperty("partyName")
    private final String partyName;
    @JsonProperty("partyType")
    private final String partyType;
    @JsonProperty("restrictDocumentDetails")
    private final String restrictDocumentDetails;
    @JsonProperty("freeTextStatements")
    private final String freeTextStatements;
    @JsonProperty("file")
    private MultipartFile file;
    @JsonProperty("documents")
    private List<Document> documents;
}
