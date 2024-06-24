package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
public class DocumentRequest {

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
     * Flag to indicate document has confidential data.
     */
    @JsonProperty("isConfidential")
    private final YesOrNo isConfidential;
    /**
     * Flag to indicate document has sensitive info.
     */
    @JsonProperty("isRestricted")
    private final YesOrNo isRestricted;
    /**
     * Explanation provided by citizen on why the document should be restricted.
     */
    @JsonProperty("restrictDocumentDetails")
    private final String restrictDocumentDetails;
    /**
     * Free text statements entered by party to generate a document.
     */
    @JsonProperty("freeTextStatements")
    private final String freeTextStatements;
    /**
     * List of documents uploaded that should be into citizen quarantine.
     */
    @JsonProperty("documents")
    private List<Document> documents;
}
