package uk.gov.hmcts.reform.prl.models.dto.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;

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

}
