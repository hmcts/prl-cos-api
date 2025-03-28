package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.edgecases.EdgeCaseTypeOfApplicationEnum;
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
    private final EdgeCaseTypeOfApplicationEnum edgeCaseTypeOfApplication;
    private final String edgeCaseTypeOfApplicationDisplayValue;
    private final String selectedCourtId;
    @JsonProperty("isEdgeCase")
    private final YesOrNo isEdgeCase;

    private final String dssCaseData;
}
