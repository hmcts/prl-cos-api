package uk.gov.hmcts.reform.prl.models.cafcass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CafcassDocumentResponse {
    private String status;
    private Resource documentResource;
}
