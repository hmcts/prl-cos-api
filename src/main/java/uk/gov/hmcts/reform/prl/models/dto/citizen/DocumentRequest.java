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
@Builder(toBuilder = true)
public class DocumentRequest {

    /**
     * Upload type from citizen dashboard(GENERATE or UPLOAD).
     */
    @JsonProperty("typeOfUpload")
    private final TypeOfDocumentUpload typeOfUpload;
    @JsonProperty("caseId")
    private final String caseId;
    /**
     * categoryId to retrieve case file view category details.
     */
    @JsonProperty("categoryId")
    private final String categoryId;
    /**
     * partyId(UUID) of logged-in party(applicant or respondent).
     */
    @JsonProperty("partyId")
    private final String partyId;
    /**
     * Name of the logged party.
     */
    @JsonProperty("partyName")
    private final String partyName;
    /**
     * Party type i.e. applicant or respondent.
     */
    @JsonProperty("partyType")
    private final String partyType;
    /**
     * Explanation provided by citizen on why keep document as restricted.
     */
    @JsonProperty("restrictDocumentDetails")
    private final String restrictDocumentDetails;
    /**
     * Free text statements entered by party to generate a document.
     */
    @JsonProperty("freeTextStatements")
    private final String freeTextStatements;
    /**
     * Document uploaded by party.
     */
    @JsonProperty("file")
    private MultipartFile file;
    /**
     * List of documents uploaded that should be into citizen quarantine.
     */
    @JsonProperty("documents")
    private List<Document> documents;
}
