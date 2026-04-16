package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildScreeningQuestionsElements {

    @JsonProperty("sq_writtenAgreement")
    private String sqWrittenAgreement;
    @JsonProperty("sq_alternativeRoutes")
    private String sqAlternativeRoutes;
    @JsonProperty("sq_legalRepresentation")
    private String sqLegalRepresentation;
    @JsonProperty("sq_courtPermissionRequired")
    private String sqCourtPermissionRequired;
    @JsonProperty("sq_permissionsWhy")
    private List<String> sqPermissionsWhy;
    @JsonProperty("sq_courtOrderPrevent_subfield")
    private String sqCourtOrderPreventSubfield;
    @JsonProperty("sq_uploadDocument_subfield")
    private Document sqUploadDocumentSubfield;
    @JsonProperty("sqPermissionsRequest")
    private String sqPermissionsRequest;

}
