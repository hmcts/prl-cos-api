package uk.gov.hmcts.reform.prl.models.dto.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
public class CitizenDocuments {

    private String partyId;
    private String partyName;
    private String partyType;
    private String categoryId;
    private String uploadedBy;
    private LocalDateTime uploadedDate;
    private LocalDateTime reviewedDate;
    private Document document;
    private Document documentWelsh;

    // Attributes for SOA
    private List<Document> applicantSoaPack; // if personal service - CA/CB -> Either applicant pack //
    //private List<Document> unservedRespondentPack; // if personal service - CA/CB -> Either applicant pack //
    private List<Document> respondentSoaPack;
    private String servedParty;
    private boolean wasCafcassServed;
    private LocalDateTime servedDate;
    private boolean isNew;
    private boolean isFinal;
}
