package uk.gov.hmcts.reform.prl.services.document.pdf;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PdfGenerationRequest {
    private final String sourceFilename;
    private final byte[] fileContent;
    private final String authToken;
}
