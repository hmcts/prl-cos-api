package uk.gov.hmcts.reform.prl.models.dto.docmosis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocmosisRenderRequest {
    private String templateName;
    private String outputName;
    private Map<String, Object> data;
}
