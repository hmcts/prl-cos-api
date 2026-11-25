package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RenameDocument {

    private String newNameForDocument;

    @JsonProperty("renameDocumentsList")
    private DynamicList renameDocumentsList;

    @JsonProperty("categoryDocumentsList")
    private DynamicList categoryDocumentsList;

    @JsonProperty("renameListDocSelected")
    private String renameListDocSelected;

}
