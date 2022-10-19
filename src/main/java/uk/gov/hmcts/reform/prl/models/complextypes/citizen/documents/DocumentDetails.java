package uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class DocumentDetails {
    private final String documentName;
    private final String documentUploadedDate;
}
