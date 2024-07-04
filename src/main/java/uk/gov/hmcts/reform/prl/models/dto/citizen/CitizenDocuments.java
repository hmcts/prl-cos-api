package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private List<Document> respondentSoaPack;
    private String servedParty;
    private String whoIsResponsible;
    private boolean wasCafcassServed;
    private boolean wasCafcassCymruServed;
    private boolean isPersonalService;

    private String orderType;
    private LocalDate createdDate;
    private LocalDate madeDate;
    private LocalDateTime servedDateTime;
    private boolean isNew;
    private boolean isFinal;
    private boolean isMultiple;
    private String documentLanguage;
}
