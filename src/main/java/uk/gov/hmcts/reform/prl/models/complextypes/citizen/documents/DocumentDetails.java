package uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DocumentDetails {
    private final String documentName;
    private final Date documentUploadedDate;
}
