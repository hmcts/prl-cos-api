package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@AllArgsConstructor
@Getter
@Data
@Builder
public class UploadedDocumentRequest {
    @JsonProperty("caseId")
    private final String caseId;
    @JsonProperty("parentDocumentType")
    private final String parentDocumentType;
    @JsonProperty("documentType")
    private final String documentType;
    @JsonProperty("partyId")
    private final String partyId;
    @JsonProperty("partyName")
    private final String partyName;
    @JsonProperty("isApplicant")
    private final String isApplicant;
    @JsonProperty("files")
    private List<MultipartFile> files;
    @JsonProperty("documentRequestedByCourt")
    private final YesOrNo documentRequestedByCourt;
}
