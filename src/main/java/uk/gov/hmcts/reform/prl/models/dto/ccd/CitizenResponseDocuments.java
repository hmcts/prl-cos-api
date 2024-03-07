package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CitizenResponseDocuments {

    private ResponseDocuments respondentAc8;
    private ResponseDocuments respondentBc8;
    private ResponseDocuments respondentCc8;
    private ResponseDocuments respondentDc8;
    private ResponseDocuments respondentEc8;

}
