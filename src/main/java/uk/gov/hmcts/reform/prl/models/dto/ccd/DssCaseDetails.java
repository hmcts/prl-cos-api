package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private final List<Element<Document>> dssApplicationFormDocuments;
    private final List<Element<Document>> dssAdditionalDocuments;
    private final String edgeCaseTypeOfApplication;
    private final String selectedCourtId;

    private final String dssCaseData;
}
