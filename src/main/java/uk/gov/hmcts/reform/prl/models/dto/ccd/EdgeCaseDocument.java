package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.reform.ccd.client.model.Document;

@Data
@NoArgsConstructor
@ToString
public class EdgeCaseDocument {

    private Document documentLink;
}
