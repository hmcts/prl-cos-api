package uk.gov.hmcts.reform.prl.models.dto.citizen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@Getter
public class UploadedDocumentRequest {
    private final String caseId;
    private final String parentDocumentType;
    private final String documentType;
    private final String partyId;
    private final String partyName;
    private final String isApplicant;
    private List<MultipartFile> files;
}
